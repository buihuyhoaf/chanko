package com.hoabh.chanko.core.network

import java.io.IOException
import java.io.InterruptedIOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.Response

@Serializable
data class ApiResponse<T>(val data: T, val meta: ResponseMeta? = null)
@Serializable
data class PageInfo(val next_cursor: String? = null, val has_more: Boolean)
@Serializable
data class ApiListResponse<T>(val data: List<T>, val page: PageInfo, val meta: ResponseMeta? = null)
@Serializable
data class ResponseMeta(val request_id: String? = null)
@Serializable
data class ErrorDetail(val field: String? = null, val reason: String? = null)
@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: List<ErrorDetail> = emptyList(),
    val retryable: Boolean = false,
)
@Serializable
data class ErrorEnvelope(val error: ApiError, val meta: ResponseMeta? = null)

sealed interface NetworkResult<out T> {
    data class Success<T>(val body: T?, val status: Int) : NetworkResult<T>
    data class HttpFailure(
        val status: Int,
        val error: ApiError?,
        val requestId: String?,
        /** Raw HTTP delta-seconds or HTTP-date; advisory only, never schedules a retry. */
        val retryAfter: String?,
    ) : NetworkResult<Nothing>
    data object NotConfigured : NetworkResult<Nothing>
    data object Timeout : NetworkResult<Nothing>
    data object ConnectionFailure : NetworkResult<Nothing>
    data object InvalidResponse : NetworkResult<Nothing>
}

/** No application retry. Cancellation propagates; transport failure never implies mutation success. */
suspend fun <T> networkRequest(json: Json, request: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = request()
        if (response.isSuccessful) {
            NetworkResult.Success(response.body(), response.code())
        } else {
            val envelope = response.errorBody()?.use { body ->
                try {
                    // Bound error parsing; raw bodies and exceptions are never retained/logged.
                    val source = body.source()
                    source.request(65_537)
                    if (source.buffer.size > 65_536) null
                    else json.decodeFromString<ErrorEnvelope>(source.readUtf8())
                } catch (_: SerializationException) { null }
            }
            NetworkResult.HttpFailure(response.code(), envelope?.error,
                envelope?.meta?.request_id, response.headers()["Retry-After"])
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: NetworkNotConfiguredException) {
        NetworkResult.NotConfigured
    } catch (_: SerializationException) {
        NetworkResult.InvalidResponse
    } catch (_: InterruptedIOException) {
        NetworkResult.Timeout
    } catch (_: IOException) {
        NetworkResult.ConnectionFailure
    }
}
