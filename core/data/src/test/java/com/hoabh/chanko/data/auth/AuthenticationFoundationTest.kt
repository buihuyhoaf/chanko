package com.hoabh.chanko.data.auth

import com.hoabh.chanko.domain.auth.*
import com.hoabh.chanko.domain.repository.foundation.AccountScope
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AuthenticationFoundationTest {
    private var now = 10000000L
    private val account = AccountScope("server-account")
    private fun credentials(owner: AccountScope = account, access: String = "access", refresh: String = "refresh",
                            created: Long = now, refreshed: Long = now, issued: Long = now,
                            expires: Long = now + SessionCredentials.ACCESS_MILLIS) =
        SessionCredentials(owner, access, refresh, issued, expires, created, refreshed)
    private val store = MemoryStore()
    private val backend = Backend()
    private val repository = AuthenticationRepositoryImpl(backend, store, AuthClock { now })
    private val provider = GoogleTokenProvider { nonce -> assertEquals("nonce", nonce); "google-proof" }
    private inner class Backend : AuthBackend {
        var exchanges = 0
        var rotations = 0
        var revocations = 0
        var challenge = SignInChallenge("id", "nonce", now + 5000)
        var exchange: AuthResult<SessionCredentials>? = null
        var rotate: AuthResult<SessionCredentials>? = null
        var revoke: AuthResult<Unit> = AuthResult.Failure(AuthFailure.NETWORK)
        var cancelRotation = false
        override suspend fun challenge() = AuthResult.Success(challenge)
        override suspend fun exchange(challenge: SignInChallenge, googleIdToken: String): AuthResult<SessionCredentials> {
            exchanges++; assertEquals("google-proof", googleIdToken)
            return exchange ?: AuthResult.Success(credentials())
        }
        override suspend fun rotate(credentials: SessionCredentials): AuthResult<SessionCredentials> {
            rotations++
            assertNull(store.saved) // old refresh cannot survive a request with uncertain outcome
            if (cancelRotation) throw CancellationException()
            return rotate ?: AuthResult.Success(credentials(refresh = "rotated", created = credentials.createdAt))
        }
        override suspend fun revoke(credentials: SessionCredentials): AuthResult<Unit> {
            revocations++; assertNull(store.saved); return revoke
        }
    }
    @Test fun challengeBindsNonceAndStoresBeforeAccountPublication() = runTest {
        assertEquals(AuthResult.Success(Unit), repository.signIn(provider))
        assertEquals(SessionState.Available(account), repository.session.value)
        assertEquals("refresh", store.saved?.refresh)
        assertFalse(store.saved.toString().contains("refresh"))
    }
    @Test fun expiredChallengeNeverReachesGoogleOrExchange() = runTest {
        backend.challenge = SignInChallenge("id", "nonce", now)
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository.signIn(GoogleTokenProvider { error("called") }))
        assertEquals(0, backend.exchanges)
    }
    @Test fun challengeExpiryDuringGoogleDoesNotExchange() = runTest {
        assertEquals(AuthResult.Failure(AuthFailure.REJECTED), repository.signIn(GoogleTokenProvider { now += 5000; "proof" }))
        assertEquals(0, backend.exchanges)
    }
    @Test fun googleCancelAndBackendRejectCannotAuthenticate() = runTest {
        assertTrue(repository.signIn(GoogleTokenProvider { null }) is AuthResult.Failure)
        backend.exchange = AuthResult.Failure(AuthFailure.REJECTED)
        assertEquals(AuthResult.Failure(AuthFailure.REJECTED), repository.signIn(provider))
        assertNull(store.saved); assertEquals(SessionState.SignedOut, repository.session.value)
    }
    @Test fun invalidTokenLifetimeCannotPersist() = runTest {
        backend.exchange = AuthResult.Success(credentials(expires = now + SessionCredentials.ACCESS_MILLIS + 1))
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository.signIn(provider))
        assertNull(store.saved)
    }
    @Test fun storageFailureNeverPublishesAccount() = runTest {
        store.failWrite = true
        assertEquals(AuthResult.Failure(AuthFailure.STORAGE), repository.signIn(provider))
        assertEquals(SessionState.SignedOut, repository.session.value)
    }
    @Test fun restoreAndExactIdleAbsoluteExpiry() = runTest {
        store.saved = credentials()
        repository.restore(); assertEquals(SessionState.Available(account), repository.session.value)
        now += SessionCredentials.IDLE_MILLIS
        repository.restore(); assertNull(store.saved); assertEquals(SessionState.SignedOut, repository.session.value)
        store.saved = credentials(created = now - SessionCredentials.ABSOLUTE_MILLIS)
        repository.restore(); assertNull(store.saved)
    }
    @Test fun expiredAccessDoesNotExtendRefreshLifetime() = runTest {
        store.saved = credentials(expires = now + 1)
        now += 2
        repository.restore()
        assertEquals(SessionState.Available(account), repository.session.value)
        assertEquals(AuthResult.Success(Unit), repository.refresh())
        assertEquals(1, backend.rotations)
    }
    @Test fun concurrentRefreshRotatesOnceAndPreservesAbsoluteAnchor() = runTest {
        repository.signIn(provider)
        val created = now
        now += SessionCredentials.ACCESS_MILLIS
        val results = List(4) { async { repository.refresh() } }.map { it.await() }
        assertTrue(results.all { it is AuthResult.Success })
        assertEquals(1, backend.rotations); assertEquals(created, store.saved?.createdAt)
    }
    @Test fun lostRotationResponseRequiresSignInWithoutReplay() = runTest {
        repository.signIn(provider); now += SessionCredentials.ACCESS_MILLIS
        backend.rotate = AuthResult.Failure(AuthFailure.TIMEOUT)
        assertEquals(AuthResult.Failure(AuthFailure.TIMEOUT), repository.refresh())
        repository.refresh()
        assertEquals(1, backend.rotations); assertNull(store.saved)
        assertEquals(SessionState.SignedOut, repository.session.value)
    }
    @Test fun cancelledRotationCannotReplayAfterRestart() = runTest {
        repository.signIn(provider); now += SessionCredentials.ACCESS_MILLIS
        backend.cancelRotation = true
        try { repository.refresh(); fail() } catch (_: CancellationException) { }
        assertNull(store.saved); repository.restore()
        assertEquals(SessionState.SignedOut, repository.session.value)
    }
    @Test fun rotationRejectsOtherAccountOrExtendedAbsoluteLifetime() = runTest {
        repository.signIn(provider); now += SessionCredentials.ACCESS_MILLIS
        backend.rotate = AuthResult.Success(credentials(owner = AccountScope("other"), refresh = "next"))
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository.refresh())
        assertNull(store.saved)
        backend.challenge = SignInChallenge("id", "nonce", now + 5000)
        repository.signIn(provider); now += SessionCredentials.ACCESS_MILLIS
        backend.rotate = AuthResult.Success(credentials(refresh = "next", created = now))
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository.refresh())
    }
    @Test fun logoutOfflineDoesNotClaimServerRevocationAndRetainsBusinessState() = runTest {
        // Separate account partitions model existing business data; auth has no data deletion dependency.
        val business = mapOf(account to listOf("draft", "pending-outbox"))
        repository.signIn(provider)
        assertEquals(AuthResult.Success(Revocation.NOT_CONFIRMED), repository.logout())
        assertEquals(SessionState.SignedOut, repository.session.value); assertNull(store.saved)
        assertEquals(listOf("draft", "pending-outbox"), business[account])
    }
    @Test fun confirmedLogoutAndAccountSwitchDoNotReusePreviousIdentity() = runTest {
        repository.signIn(provider)
        assertEquals(AuthResult.Failure(AuthFailure.REJECTED), repository.signIn(provider))
        backend.revoke = AuthResult.Success(Unit)
        assertEquals(AuthResult.Success(Revocation.CONFIRMED), repository.logout())
        val other = AccountScope("other")
        backend.exchange = AuthResult.Success(credentials(owner = other))
        repository.signIn(provider)
        assertEquals(SessionState.Available(other), repository.session.value)
    }
    @Test fun failedCredentialDeletionReportsFailureWithoutServerClaim() = runTest {
        repository.signIn(provider); store.failClear = true
        assertEquals(AuthResult.Failure(AuthFailure.STORAGE), repository.logout())
        assertEquals(0, backend.revocations); assertEquals(SessionState.SignedOut, repository.session.value)
    }
    @Test fun logoutStillClearsCorruptCredentials() = runTest {
        repository.signIn(provider); store.failRead = true
        assertEquals(AuthResult.Success(Revocation.NOT_CONFIRMED), repository.logout())
        assertNull(store.saved); assertEquals(0, backend.revocations)
    }
    @Test fun corruptedStorageFailsClosed() = runTest {
        store.failRead = true
        assertEquals(AuthResult.Failure(AuthFailure.STORAGE), repository.restore())
        assertEquals(SessionState.SignedOut, repository.session.value)
    }
    @Test fun useCaseEmitsExplicitLoadingThenSuccessOrFailure() = runTest {
        val useCase = SignInWithGoogleUseCase(repository)
        assertEquals(listOf(AuthOperation.Loading, AuthOperation.Success(Unit)), useCase.execute(provider).toList())
        repository.logout()
        backend.exchange = AuthResult.Failure(AuthFailure.REJECTED)
        assertEquals(listOf(AuthOperation.Loading, AuthOperation.Failure(AuthFailure.REJECTED)), useCase.execute(provider).toList())
    }
    @Test fun unconfiguredProductionNeverRequestsGoogleProof() = runTest {
        val unconfigured = AuthenticationRepositoryImpl(UnconfiguredAuthBackend(), store, AuthClock { now })
        assertEquals(AuthResult.Failure(AuthFailure.NOT_CONFIGURED), unconfigured.signIn(GoogleTokenProvider { error("called") }))
        assertNull(store.saved)
    }
}
private class MemoryStore : SecureSessionStore {
    var saved: SessionCredentials? = null
    var failWrite = false
    var failRead = false
    var failClear = false
    override suspend fun read(): SessionCredentials? { if (failRead) throw IOException(); return saved }
    override suspend fun replace(credentials: SessionCredentials) { if (failWrite) throw IOException(); saved = credentials }
    override suspend fun clear() { if (failClear) throw IOException(); saved = null }
}
