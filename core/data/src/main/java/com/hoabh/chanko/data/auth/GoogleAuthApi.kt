package com.hoabh.chanko.data.auth

import com.hoabh.chanko.core.network.ApiResponse
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GoogleAuthApi {
    @POST("auth/google/challenge")
    suspend fun challenge(): Response<ApiResponse<GoogleChallengeDto>>
    @POST("auth/google")
    suspend fun signIn(@Body request: GoogleSignInRequest): Response<ApiResponse<AuthTokensDto>>
}

@Serializable
class GoogleChallengeDto(val challenge_id: String, val nonce: String, val expires_at: String) {
    override fun toString() = "GoogleChallengeDto([redacted])"
}
@Serializable
class GoogleSignInRequest(val challenge_id: String, val id_token: String) {
    override fun toString() = "GoogleSignInRequest([redacted])"
}
@Serializable
class AuthTokensDto(val access_token: String, val refresh_token: String) {
    override fun toString() = "AuthTokensDto([redacted])"
}

fun interface AuthSessionStore {
    /** Atomic, encrypted, device-local write; throw if durability cannot be guaranteed. */
    suspend fun save(accessToken: String, refreshToken: String)
}
