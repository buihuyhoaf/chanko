package com.hoabh.chanko.domain.auth

import com.hoabh.chanko.domain.repository.foundation.AccountScope
import kotlinx.coroutines.flow.StateFlow

/** No credentials in observable state. Account identity comes only from the backend. */
sealed interface SessionState {
    data object SignedOut : SessionState
    data class Available(val account: AccountScope) : SessionState
}
sealed interface AuthOperation<out T> {
    data object Loading : AuthOperation<Nothing>
    data class Success<T>(val value: T) : AuthOperation<T>
    data class Failure(val reason: AuthFailure) : AuthOperation<Nothing>
}
enum class Revocation { CONFIRMED, NOT_CONFIRMED }

interface AuthenticationRepository {
    val session: StateFlow<SessionState>
    suspend fun restore(): AuthResult<Unit>
    suspend fun signIn(provider: GoogleTokenProvider): AuthResult<Unit>
    suspend fun refresh(): AuthResult<Unit>
    /** Clears credentials only. Retains Room, drafts and any pre-existing outbox by account. */
    suspend fun logout(): AuthResult<Revocation>
}

class SignInWithGoogleUseCase(private val repository: AuthenticationRepository) {
    fun execute(provider: GoogleTokenProvider) = kotlinx.coroutines.flow.flow {
        emit(AuthOperation.Loading)
        emit(when (val result = repository.signIn(provider)) {
            is AuthResult.Success -> AuthOperation.Success(Unit)
            is AuthResult.Failure -> AuthOperation.Failure(result.reason)
        })
    }
}
class RestoreAuthSessionUseCase(private val repository: AuthenticationRepository) {
    suspend operator fun invoke() = repository.restore()
}
class RefreshAuthSessionUseCase(private val repository: AuthenticationRepository) {
    suspend operator fun invoke() = repository.refresh()
}
class LogoutUseCase(private val repository: AuthenticationRepository) {
    suspend operator fun invoke() = repository.logout()
}
