package com.hoabh.chanko.dev

import com.hoabh.chanko.domain.auth.AuthFailure
import com.hoabh.chanko.domain.auth.AuthResult
import com.hoabh.chanko.domain.auth.AuthenticationRepository
import com.hoabh.chanko.domain.auth.GoogleAuthChallenge
import com.hoabh.chanko.domain.auth.GoogleAuthRepository
import com.hoabh.chanko.domain.auth.GoogleTokenProvider
import com.hoabh.chanko.domain.auth.Revocation
import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.domain.repository.foundation.AccountScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Giả lập phiên xác thực cho môi trường phát triển (Dev).
 * Giúp lập trình viên làm việc offline hoàn toàn mà không bị chặn ở màn hình đăng nhập.
 */
@Singleton
class DevFakeAuthenticationRepository @Inject constructor() : AuthenticationRepository {
    private val devAccount = AccountScope("dev-user")
    private var hasLoggedIn = false
    private val _session = MutableStateFlow<SessionState>(SessionState.SignedOut)
    override val session: StateFlow<SessionState> = _session.asStateFlow()

    override suspend fun restore(): AuthResult<Unit> {
        if (hasLoggedIn) {
            _session.value = SessionState.Available(devAccount)
        } else {
            _session.value = SessionState.SignedOut
        }
        return AuthResult.Success(Unit)
    }

    override suspend fun signIn(provider: GoogleTokenProvider): AuthResult<Unit> {
        hasLoggedIn = true
        _session.value = SessionState.Available(devAccount)
        return AuthResult.Success(Unit)
    }

    override suspend fun refresh(): AuthResult<Unit> {
        return AuthResult.Success(Unit)
    }

    override suspend fun logout(): AuthResult<Revocation> {
        hasLoggedIn = false
        _session.value = SessionState.SignedOut
        DevSetupGateway.reset()
        return AuthResult.Success(Revocation.CONFIRMED)
    }
}

/**
 * Giả lập kho lưu trữ Google Auth ở tầng legacy cho môi trường dev.
 */
class DevFakeGoogleAuthRepository : GoogleAuthRepository {
    override suspend fun createChallenge(): AuthResult<GoogleAuthChallenge> {
        return AuthResult.Success(
            GoogleAuthChallenge(
                id = "dev-challenge-id",
                nonce = "dev-nonce-12345",
                expiresAt = "2099-01-01T00:00:00Z"
            )
        )
    }

    override suspend fun signIn(challenge: GoogleAuthChallenge, idToken: String): AuthResult<Unit> {
        return AuthResult.Success(Unit)
    }
}
