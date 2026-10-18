package com.hoabh.chanko.core.network

import java.io.IOException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okio.BufferedSink
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** No environment is enabled until TECH-04 supplies an approved HTTPS API base URL. */
class NetworkConfig(val baseUrl: HttpUrl? = null) {
    init {
        require(baseUrl == null || (baseUrl.isHttps && baseUrl.username.isEmpty() &&
            baseUrl.password.isEmpty() && baseUrl.query == null && baseUrl.fragment == null &&
            baseUrl.encodedPath.endsWith("/"))) { "Expected an HTTPS base URL ending in / without credentials, query or fragment" }
    }
}

class NetworkNotConfiguredException : IOException("Network environment is not configured")

object NetworkClient {
    fun json(): Json = Json { ignoreUnknownKeys = true }

    fun httpClient(config: NetworkConfig): OkHttpClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(false)
        .followRedirects(false)
        .followSslRedirects(false)
        .addInterceptor { chain ->
            val base = config.baseUrl ?: throw NetworkNotConfiguredException()
            val url = chain.request().url
            if (url.scheme != base.scheme || url.host != base.host || url.port != base.port ||
                !url.encodedPath.startsWith(base.encodedPath)) {
                throw IOException("Request is outside the configured API base")
            }
            val request = chain.request()
            val body = request.body
            // OkHttp may follow 503 Retry-After: 0 even with connection retries disabled.
            // One-shot mutation bodies preserve the first response without replaying a write.
            val guarded = if (body != null && request.method != "GET" && request.method != "HEAD") {
                request.newBuilder().method(request.method, object : RequestBody() {
                    override fun contentType() = body.contentType()
                    override fun contentLength() = body.contentLength()
                    override fun isOneShot() = true
                    override fun writeTo(sink: BufferedSink) = body.writeTo(sink)
                }).build()
            } else request
            chain.proceed(guarded)
        }
        .build()

    fun retrofit(config: NetworkConfig, client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            // Reserved .invalid address is never contacted: the interceptor fails before I/O.
            .baseUrl(config.baseUrl ?: "https://unconfigured.invalid/api/v1/".toHttpUrl())
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
