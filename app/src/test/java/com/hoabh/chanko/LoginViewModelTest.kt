package com.hoabh.chanko

import com.hoabh.chanko.domain.auth.*
import com.hoabh.chanko.presentation.screens.login.LoginUiEffect
import com.hoabh.chanko.presentation.screens.login.LoginViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun onlyBackendAcceptanceCanNavigate() = runTest(dispatcher) {
        val repo = FakeAuth(); val vm = LoginViewModel(repo)
        val gate = CompletableDeferred<Unit>()
        repo.beforeSignIn = { gate.await() }
        vm.loginWithGoogle { nonce -> assertEquals("nonce-1", nonce); "google-proof" }
        runCurrent()
        assertTrue(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isLoginSuccess)
        assertEquals(listOf("challenge", "exchange:id-1:google-proof"), repo.calls)
        gate.complete(Unit); runCurrent()
        assertTrue(vm.uiState.value.isLoginSuccess)
        assertFalse(vm.uiState.value.isLoading)
    }
    @Test fun doubleTapStartsOnlyOneChallenge() = runTest(dispatcher) {
        val repo = FakeAuth(); val vm = LoginViewModel(repo)
        val gate = CompletableDeferred<String?>()
        vm.loginWithGoogle { gate.await() }; vm.loginWithGoogle { error("Duplicate") }; runCurrent()
        assertEquals(listOf("challenge"), repo.calls)
        vm.cancelLogin(); runCurrent()
        assertFalse(vm.uiState.value.isLoading)
    }
    @Test fun cancelledPickerDoesNotExchangeAndUsesFoundationFailure() = runTest(dispatcher) {
        val repo = FakeAuth(); val vm = LoginViewModel(repo)
        vm.loginWithGoogle { null }; runCurrent()
        assertEquals(listOf("challenge"), repo.calls)
        assertFalse(vm.uiState.value.isLoginSuccess); assertEquals(AuthFailure.GOOGLE, vm.uiState.value.error)
    }
    @Test fun unavailableConfigurationNeverLaunchesGoogle() = runTest(dispatcher) {
        val repo = FakeAuth(); repo.challengeFailure = AuthFailure.NOT_CONFIGURED
        val vm = LoginViewModel(repo)
        vm.loginWithGoogle { error("Should not launch") }; runCurrent()
        assertEquals(AuthFailure.NOT_CONFIGURED, vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoginSuccess)
    }
    @Test fun rejectionAndRetryGetNewChallengeAndNonce() = runTest(dispatcher) {
        val repo = FakeAuth(); repo.exchangeFailure = AuthFailure.REJECTED
        val vm = LoginViewModel(repo)
        vm.loginWithGoogle { "bad" }; runCurrent()
        assertEquals(AuthFailure.REJECTED, vm.uiState.value.error)
        repo.exchangeFailure = null
        vm.loginWithGoogle { nonce -> assertEquals("nonce-2", nonce); "good" }; runCurrent()
        assertTrue(vm.uiState.value.isLoginSuccess); assertNull(vm.uiState.value.error)
    }
    @Test fun providerFailureCanRetryWithoutLeakingExceptionText() = runTest(dispatcher) {
        val vm = LoginViewModel(FakeAuth())
        vm.loginWithGoogle { throw IllegalStateException("sensitive-token") }; runCurrent()
        assertEquals(AuthFailure.GOOGLE, vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading); assertFalse(vm.uiState.value.isLoginSuccess)
    }
    @Test fun routeCancellationPreventsLateSuccess() = runTest(dispatcher) {
        val repo = FakeAuth(); val gate = CompletableDeferred<Unit>(); repo.beforeSignIn = { gate.await() }
        val vm = LoginViewModel(repo)
        vm.loginWithGoogle { "token" }; runCurrent(); vm.cancelLogin(); runCurrent()
        gate.complete(Unit); runCurrent()
        assertFalse(vm.uiState.value.isLoginSuccess); assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test fun loginSuccessEmitsNavigateToHomeEffect() = runTest(dispatcher) {
        val repo = FakeAuth()
        val vm = LoginViewModel(repo)

        val effectDeferred = async { vm.effect.first() }
        vm.loginWithGoogle { "valid-token" }
        runCurrent()

        assertEquals(LoginUiEffect.NavigateToHome, effectDeferred.await())
    }
}
private class FakeAuth : AuthenticationRepository {
    val calls = mutableListOf<String>()
    var challengeFailure: AuthFailure? = null
    var exchangeFailure: AuthFailure? = null
    var beforeSignIn: suspend () -> Unit = {}
    var sequence = 0
    override val session = kotlinx.coroutines.flow.MutableStateFlow<SessionState>(SessionState.SignedOut)
    override suspend fun restore() = AuthResult.Success(Unit)
    override suspend fun refresh() = AuthResult.Success(Unit)
    override suspend fun logout() = AuthResult.Success(Revocation.CONFIRMED)
    override suspend fun signIn(provider: GoogleTokenProvider): AuthResult<Unit> {
        val challenge = createChallenge()
        if (challenge is AuthResult.Failure) return challenge
        val token = provider.getIdToken((challenge as AuthResult.Success).value.nonce)
            ?: return AuthResult.Failure(AuthFailure.GOOGLE)
        return signIn(challenge.value, token)
    }
    suspend fun createChallenge(): AuthResult<GoogleAuthChallenge> {
        calls += "challenge"; sequence++
        return challengeFailure?.let { AuthResult.Failure(it) }
            ?: AuthResult.Success(GoogleAuthChallenge("id-$sequence", "nonce-$sequence", "future"))
    }
    suspend fun signIn(challenge: GoogleAuthChallenge, idToken: String): AuthResult<Unit> {
        calls += "exchange:${challenge.id}:$idToken"; beforeSignIn()
        return exchangeFailure?.let { AuthResult.Failure(it) } ?: AuthResult.Success(Unit)
    }
}
