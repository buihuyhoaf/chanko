package com.hoabh.chanko.data.auth

import com.hoabh.chanko.domain.auth.*
import com.hoabh.chanko.domain.repository.foundation.AccountScope

/** Internal models, NOT wire schemas. A reviewed adapter must map the approved backend contract. */
class SessionCredentials(
    val account: AccountScope,
    val access: String,
    val refresh: String,
    val issuedAt: Long,
    val accessExpiresAt: Long,
    val createdAt: Long,
    val refreshedAt: Long,
) {
    override fun toString() = "SessionCredentials([redacted])"
    fun valid(now: Long): Boolean = access.isNotBlank() && refresh.isNotBlank() &&
        createdAt >= 0 && createdAt <= refreshedAt && refreshedAt <= issuedAt && issuedAt <= now &&
        accessExpiresAt > issuedAt && accessExpiresAt - issuedAt <= ACCESS_MILLIS &&
        now - refreshedAt < IDLE_MILLIS && now - createdAt < ABSOLUTE_MILLIS
    companion object {
        const val ACCESS_MILLIS = 15L * 60 * 1000
        const val IDLE_MILLIS = 30L * 24 * 60 * 60 * 1000
        const val ABSOLUTE_MILLIS = 90L * 24 * 60 * 60 * 1000
    }
}
class SignInChallenge(val id: String, val nonce: String, val expiresAt: Long) {
    override fun toString() = "SignInChallenge([redacted])"
}
interface AuthBackend {
    suspend fun challenge(): AuthResult<SignInChallenge>
    /** Server verifies Google signature/issuer/audience/expiry/nonce and atomically consumes challenge. */
    suspend fun exchange(challenge: SignInChallenge, googleIdToken: String): AuthResult<SessionCredentials>
    /** Server rotates atomically, detects reuse, enforces idle30d/absolute90d. Never blindly retry. */
    suspend fun rotate(credentials: SessionCredentials): AuthResult<SessionCredentials>
    suspend fun revoke(credentials: SessionCredentials): AuthResult<Unit>
}
/** Durable atomic encrypted replacement outside Room, excluded from backup. Never silently recover corruption. */
interface SecureSessionStore {
    suspend fun read(): SessionCredentials?
    suspend fun replace(credentials: SessionCredentials)
    suspend fun clear()
}
fun interface AuthClock { fun nowMillis(): Long }

/** Fail-closed production binding until wire contract, environment and credentials are approved. */
class UnconfiguredAuthBackend : AuthBackend {
    override suspend fun challenge() = AuthResult.Failure(AuthFailure.NOT_CONFIGURED)
    override suspend fun exchange(challenge: SignInChallenge, googleIdToken: String) = AuthResult.Failure(AuthFailure.NOT_CONFIGURED)
    override suspend fun rotate(credentials: SessionCredentials) = AuthResult.Failure(AuthFailure.NOT_CONFIGURED)
    override suspend fun revoke(credentials: SessionCredentials) = AuthResult.Failure(AuthFailure.NOT_CONFIGURED)
}
