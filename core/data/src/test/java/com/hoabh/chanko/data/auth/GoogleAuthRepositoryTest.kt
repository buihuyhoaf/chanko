package com.hoabh.chanko.data.auth

import com.hoabh.chanko.core.network.ApiResponse
import com.hoabh.chanko.domain.auth.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

class GoogleAuthRepositoryTest {
    private val api = FakeApi()
    private var stored = 0
    private var failStorage = false
    private val store = AuthSessionStore { access, refresh ->
        if (failStorage) throw IOException()
        assertEquals("access", access); assertEquals("refresh", refresh); stored++
    }
    private fun repo(configured: Boolean = true) = GoogleAuthRepositoryImpl(api, Json { ignoreUnknownKeys = true }, store, configured)
    private val challenge = GoogleAuthChallenge("challenge", "nonce", "future")

    @Test fun missingConfigurationPerformsNoIo() = runTest {
        assertEquals(AuthResult.Failure(AuthFailure.NOT_CONFIGURED), repo(false).createChallenge())
        assertEquals(0, api.calls)
    }
    @Test fun challengeRetainsServerNonceBinding() = runTest {
        val result = repo().createChallenge() as AuthResult.Success
        assertEquals("nonce", result.value.nonce)
        assertEquals("challenge", result.value.id)
    }
    @Test fun nullOrMalformedChallengeFailsClosed() = runTest {
        api.challengeResponse = Response.success(ApiResponse(GoogleChallengeDto("", "", "")))
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repo().createChallenge())
        api.challengeResponse = Response.success(null)
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repo().createChallenge())
    }
    @Test fun backendTokensAreStoredBeforeSuccess() = runTest {
        assertEquals(AuthResult.Success(Unit), repo().signIn(challenge, "google-token"))
        assertEquals(1, stored)
        assertEquals("challenge", api.request?.challenge_id)
        assertEquals("google-token", api.request?.id_token)
        assertFalse(api.request.toString().contains("google-token"))
    }
    @Test fun missingTokensNeverStoreOrAuthenticate() = runTest {
        api.tokensResponse = Response.success(ApiResponse(AuthTokensDto("access", "")))
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repo().signIn(challenge, "token"))
        assertEquals(0, stored)
    }
    @Test fun rejectedOrRateLimitedResponseDoesNotAuthenticate() = runTest {
        api.tokensResponse = Response.error(401, "{}".toResponseBody())
        assertEquals(AuthResult.Failure(AuthFailure.REJECTED), repo().signIn(challenge, "token"))
        api.tokensResponse = Response.error(429, "{}".toResponseBody())
        assertEquals(AuthResult.Failure(AuthFailure.RATE_LIMITED), repo().signIn(challenge, "token"))
        assertEquals(0, stored)
    }
    @Test fun lostResponseDoesNotRetryOrStore() = runTest {
        api.failure = IOException()
        assertEquals(AuthResult.Failure(AuthFailure.NETWORK), repo().signIn(challenge, "token"))
        assertEquals(1, api.calls); assertEquals(0, stored)
    }
    @Test fun storageFailureDoesNotAuthenticate() = runTest {
        failStorage = true
        assertEquals(AuthResult.Failure(AuthFailure.STORAGE), repo().signIn(challenge, "token"))
    }
    @Test fun cancellationPropagates() = runTest {
        api.failure = CancellationException()
        try { repo().createChallenge(); fail("Cancellation expected") } catch (_: CancellationException) { }
        assertEquals(0, stored)
    }
}
private class FakeApi : GoogleAuthApi {
    var calls = 0
    var request: GoogleSignInRequest? = null
    var failure: Exception? = null
    var challengeResponse: Response<ApiResponse<GoogleChallengeDto>> = Response.success(ApiResponse(GoogleChallengeDto("challenge", "nonce", "future")))
    var tokensResponse: Response<ApiResponse<AuthTokensDto>> = Response.success(ApiResponse(AuthTokensDto("access", "refresh")))
    override suspend fun challenge(): Response<ApiResponse<GoogleChallengeDto>> {
        calls++; failure?.let { throw it }; return challengeResponse
    }
    override suspend fun signIn(request: GoogleSignInRequest): Response<ApiResponse<AuthTokensDto>> {
        calls++; this.request = request; failure?.let { throw it }; return tokensResponse
    }
}
