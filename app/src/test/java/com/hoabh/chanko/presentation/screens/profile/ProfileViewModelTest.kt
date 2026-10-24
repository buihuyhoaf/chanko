package com.hoabh.chanko.presentation.screens.profile

import com.hoabh.chanko.domain.auth.AuthResult
import com.hoabh.chanko.domain.auth.AuthenticationRepository
import com.hoabh.chanko.domain.auth.GoogleTokenProvider
import com.hoabh.chanko.domain.auth.LogoutUseCase
import com.hoabh.chanko.domain.auth.Revocation
import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.domain.repository.foundation.AccountScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeAuthRepository : AuthenticationRepository {
    private val _session = MutableStateFlow<SessionState>(SessionState.SignedOut)
    override val session: StateFlow<SessionState> = _session

    fun setSession(state: SessionState) {
        _session.value = state
    }

    override suspend fun restore(): AuthResult<Unit> = AuthResult.Success(Unit)
    override suspend fun signIn(provider: GoogleTokenProvider): AuthResult<Unit> = AuthResult.Success(Unit)
    override suspend fun refresh(): AuthResult<Unit> = AuthResult.Success(Unit)
    override suspend fun logout(): AuthResult<Revocation> = AuthResult.Success(Revocation.CONFIRMED)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        logoutUseCase = LogoutUseCase(authRepository)
        viewModel = ProfileViewModel(authRepository, logoutUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observing authenticated session updates user account info`() = runTest {
        authRepository.setSession(SessionState.Available(AccountScope("hoa.nguyen@chanko.app")))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("hoa.nguyen@chanko.app", state.account.email)
        assertEquals("hoa.nguyen@chanko.app", state.account.displayName)
        assertTrue(state.account.isGoogleLinked)
    }

    @Test
    fun `toggle ready for review updates preference state`() {
        assertTrue(viewModel.uiState.value.isReadyForReviewNotificationEnabled)

        viewModel.onEvent(ProfileUiEvent.OnReadyForReviewToggled(false))
        assertFalse(viewModel.uiState.value.isReadyForReviewNotificationEnabled)

        viewModel.onEvent(ProfileUiEvent.OnReadyForReviewToggled(true))
        assertTrue(viewModel.uiState.value.isReadyForReviewNotificationEnabled)
    }

    @Test
    fun `back click sends NavigateBack effect`() = runTest {
        viewModel.onEvent(ProfileUiEvent.OnBackClicked)

        val effect = viewModel.effect.first()
        assertEquals(ProfileUiEffect.NavigateBack, effect)
    }

    @Test
    fun `personality row click sends NavigateToPersonality effect`() = runTest {
        viewModel.onEvent(ProfileUiEvent.OnPersonalityRowClicked)

        val effect = viewModel.effect.first()
        assertEquals(ProfileUiEffect.NavigateToPersonality, effect)
    }

    @Test
    fun `confirm sign out triggers logout and sends NavigateToLogin effect`() = runTest {
        viewModel.onEvent(ProfileUiEvent.OnConfirmSignOut)
        advanceUntilIdle()

        val effect = viewModel.effect.first()
        assertEquals(ProfileUiEffect.NavigateToLogin, effect)
        assertFalse(viewModel.uiState.value.isSignOutInProgress)
    }
}
