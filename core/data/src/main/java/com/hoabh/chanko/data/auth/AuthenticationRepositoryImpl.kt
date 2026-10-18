package com.hoabh.chanko.data.auth

import com.hoabh.chanko.domain.auth.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Serializes credential mutations. No HTTP authenticator, background sync or automatic retries. */
class AuthenticationRepositoryImpl(
    private val backend: AuthBackend,
    private val store: SecureSessionStore,
    private val clock: AuthClock,
) : AuthenticationRepository {
    private val lock = Mutex()
    private val state = MutableStateFlow<SessionState>(SessionState.SignedOut)
    override val session = state.asStateFlow()

    override suspend fun restore(): AuthResult<Unit> = lock.withLock {
        state.value = SessionState.SignedOut
        storage {
            val saved = store.read()
            if (saved != null) {
                if (saved.valid(clock.nowMillis())) state.value = SessionState.Available(saved.account)
                else store.clear()
            }
            AuthResult.Success(Unit)
        }
    }

    override suspend fun signIn(provider: GoogleTokenProvider): AuthResult<Unit> = lock.withLock {
        // Account switching requires explicit logout; never expose previous account during a new login.
        if (state.value is SessionState.Available) return@withLock AuthResult.Failure(AuthFailure.REJECTED)
        val cleared = storage { clear(); AuthResult.Success(Unit) }
        if (cleared is AuthResult.Failure) return@withLock cleared
        val result = backend.challenge()
        if (result is AuthResult.Failure) return@withLock result
        val challenge = (result as AuthResult.Success).value
        if (challenge.id.isBlank() || challenge.nonce.isBlank() || challenge.expiresAt <= clock.nowMillis())
            return@withLock AuthResult.Failure(AuthFailure.INVALID_RESPONSE)
        val token = try { provider.getIdToken(challenge.nonce) }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { return@withLock AuthResult.Failure(AuthFailure.GOOGLE) }
        if (token.isNullOrBlank()) return@withLock AuthResult.Failure(AuthFailure.GOOGLE)
        if (challenge.expiresAt <= clock.nowMillis()) return@withLock AuthResult.Failure(AuthFailure.REJECTED)
        when (val exchanged = backend.exchange(challenge, token)) {
            is AuthResult.Failure -> exchanged
            is AuthResult.Success -> persist(exchanged.value)
        }
    }

    override suspend fun refresh(): AuthResult<Unit> = lock.withLock {
        storage {
            val old = store.read() ?: return@storage AuthResult.Failure(AuthFailure.REJECTED)
            if (state.value != SessionState.Available(old.account) || !old.valid(clock.nowMillis())) {
                clear(); return@storage AuthResult.Failure(AuthFailure.REJECTED)
            }
            // Queued callers see the newly persisted token and do not rotate it again.
            if (old.accessExpiresAt > clock.nowMillis()) return@storage AuthResult.Success(Unit)
            // Remove durable refresh BEFORE sending it: cancellation, crash or lost response cannot replay it.
            clear()
            when (val rotated = backend.rotate(old)) {
                is AuthResult.Failure -> rotated
                is AuthResult.Success -> {
                    val next = rotated.value
                    if (next.account != old.account || next.createdAt != old.createdAt ||
                        next.refreshedAt < old.refreshedAt || next.refresh == old.refresh)
                        AuthResult.Failure(AuthFailure.INVALID_RESPONSE)
                    else persist(next)
                }
            }
        }
    }

    override suspend fun logout(): AuthResult<Revocation> = lock.withLock {
        storage {
            val read = storage { AuthResult.Success(store.read()) }
            val old = (read as? AuthResult.Success)?.value
            // Local logout completes before attempting network. Never touch business data.
            clear()
            if (old == null) return@storage AuthResult.Success(Revocation.NOT_CONFIRMED)
            val revoked = backend.revoke(old)
            AuthResult.Success(if (revoked is AuthResult.Success) Revocation.CONFIRMED else Revocation.NOT_CONFIRMED)
        }
    }

    private suspend fun persist(value: SessionCredentials): AuthResult<Unit> {
        if (!value.valid(clock.nowMillis()) || value.accessExpiresAt <= clock.nowMillis())
            return AuthResult.Failure(AuthFailure.INVALID_RESPONSE)
        return storage {
            // Complete credential durability + state publication together even if caller cancels.
            withContext(NonCancellable) {
                store.replace(value)
                state.value = SessionState.Available(value.account)
            }
            AuthResult.Success(Unit)
        }
    }
    private suspend fun clear() = withContext(NonCancellable) {
        state.value = SessionState.SignedOut
        store.clear()
    }
    private suspend fun <T> storage(block: suspend () -> AuthResult<T>): AuthResult<T> = try { block() }
    catch (e: CancellationException) { throw e }
    catch (_: java.io.IOException) { state.value = SessionState.SignedOut; AuthResult.Failure(AuthFailure.STORAGE) }
    catch (_: java.security.GeneralSecurityException) { state.value = SessionState.SignedOut; AuthResult.Failure(AuthFailure.STORAGE) }
}
