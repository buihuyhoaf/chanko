package com.hoabh.chanko.data.auth

import com.hoabh.chanko.core.network.NetworkResult
import com.hoabh.chanko.core.network.networkRequest
import com.hoabh.chanko.domain.auth.*
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthRepositoryImpl(
    private val api: GoogleAuthApi,
    private val json: Json,
    private val sessions: AuthSessionStore,
    private val configured: Boolean,
) : GoogleAuthRepository {
    override suspend fun createChallenge(): AuthResult<GoogleAuthChallenge> {
        if (!configured) return AuthResult.Failure(AuthFailure.NOT_CONFIGURED)
        return when (val result = networkRequest(json) { api.challenge() }) {
            is NetworkResult.Success -> {
                val data = result.body?.data
                if (data == null || data.challenge_id.isBlank() || data.nonce.isBlank() || data.expires_at.isBlank())
                    AuthResult.Failure(AuthFailure.INVALID_RESPONSE)
                else AuthResult.Success(GoogleAuthChallenge(data.challenge_id, data.nonce, data.expires_at))
            }
            else -> AuthResult.Failure(result.failure())
        }
    }

    override suspend fun signIn(challenge: GoogleAuthChallenge, idToken: String): AuthResult<Unit> {
        if (!configured) return AuthResult.Failure(AuthFailure.NOT_CONFIGURED)
        if (challenge.id.isBlank() || challenge.nonce.isBlank() || idToken.isBlank())
            return AuthResult.Failure(AuthFailure.INVALID_RESPONSE)
        // No client-side account, PAY_NOW or purchase creation; the backend owns AUTH-05.
        return when (val result = networkRequest(json) { api.signIn(GoogleSignInRequest(challenge.id, idToken)) }) {
            is NetworkResult.Success -> {
                val data = result.body?.data
                if (data == null || data.access_token.isBlank() || data.refresh_token.isBlank()) {
                    AuthResult.Failure(AuthFailure.INVALID_RESPONSE)
                } else try {
                    sessions.save(data.access_token, data.refresh_token)
                    AuthResult.Success(Unit)
                } catch (e: CancellationException) { throw e
                } catch (_: Exception) { AuthResult.Failure(AuthFailure.STORAGE) }
            }
            else -> AuthResult.Failure(result.failure())
        }
    }
}

private fun NetworkResult<*>.failure(): AuthFailure = when (this) {
    NetworkResult.NotConfigured -> AuthFailure.NOT_CONFIGURED
    NetworkResult.ConnectionFailure -> AuthFailure.NETWORK
    NetworkResult.Timeout -> AuthFailure.TIMEOUT
    NetworkResult.InvalidResponse -> AuthFailure.INVALID_RESPONSE
    is NetworkResult.HttpFailure -> when (status) {
        400, 401, 403, 409, 410, 422 -> AuthFailure.REJECTED
        429 -> AuthFailure.RATE_LIMITED
        else -> AuthFailure.SERVER
    }
    is NetworkResult.Success -> AuthFailure.INVALID_RESPONSE
}
