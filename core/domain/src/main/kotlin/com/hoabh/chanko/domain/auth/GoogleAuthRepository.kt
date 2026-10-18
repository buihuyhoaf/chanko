package com.hoabh.chanko.domain.auth

/** One-use backend challenge. Never place nonce or ID token in UI state or logs. */
class GoogleAuthChallenge(val id: String, val nonce: String, val expiresAt: String) {
    override fun toString() = "GoogleAuthChallenge([redacted])"
}

enum class AuthFailure { NOT_CONFIGURED, NETWORK, TIMEOUT, REJECTED, RATE_LIMITED, SERVER, INVALID_RESPONSE, STORAGE, GOOGLE }
sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val reason: AuthFailure) : AuthResult<Nothing>
}

interface GoogleAuthRepository {
    suspend fun createChallenge(): AuthResult<GoogleAuthChallenge>
    /** Success means the backend accepted the Google proof and the session was stored securely. */
    suspend fun signIn(challenge: GoogleAuthChallenge, idToken: String): AuthResult<Unit>
}

fun interface GoogleTokenProvider {
    /** Must bind the backend nonce to the Google credential request. Null means user cancelled. */
    suspend fun getIdToken(nonce: String): String?
}
