package com.hoabh.chanko.core.network

import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

@Serializable
data class TestPayload(val value: String)
interface TestApi {
    @GET("fixture") suspend fun get(): Response<TestPayload>
    @GET suspend fun dynamic(@Url url: String): Response<TestPayload>
    @POST("fixture") suspend fun post(): Response<TestPayload>
}

class NetworkFoundationTest {
    private val json = NetworkClient.json()

    private fun fixture(block: suspend (TestApi, MockWebServer) -> Unit) = runTest {
        val certificate = HeldCertificate.Builder().addSubjectAlternativeName("localhost").build()
        val serverCertificates = HandshakeCertificates.Builder().heldCertificate(certificate).build()
        val clientCertificates = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()
        MockWebServer().use { server ->
            server.useHttps(serverCertificates.sslSocketFactory(), false)
            server.start()
            val config = NetworkConfig(server.url("/api/v1/"))
            val client = NetworkClient.httpClient(config).newBuilder()
                .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager).build()
            try {
                block(NetworkClient.retrofit(config, client, json).create(TestApi::class.java), server)
            } finally {
                client.connectionPool.evictAll()
                client.dispatcher.executorService.shutdown()
            }
        }
    }

    @Test fun `JSON converter ignores additive fields and uses base path`() = fixture { api, server ->
        server.enqueue(MockResponse().setBody("""{"value":"ok","extra":1}"""))
        val result = networkRequest(json) { api.get() } as NetworkResult.Success
        assertEquals(TestPayload("ok"), result.body)
        assertEquals("/api/v1/fixture", server.takeRequest().path)
    }

    @Test fun `HTTP codes remain failures with optional error details`() = fixture { api, server ->
        for (status in listOf(400,401,403,404,409,422,428,429,500,503,504)) {
            server.enqueue(MockResponse().setResponseCode(status).setHeader("Retry-After", "30")
                .setBody("""{"error":{"code":"UNKNOWN_NEW_CODE","message":"failure","retryable":true,"details":[{"field":"version","reason":"stale"}]},"meta":{"request_id":"r1"}}"""))
            val result = networkRequest(json) { api.post() } as NetworkResult.HttpFailure
            assertEquals(status, result.status)
            assertEquals("UNKNOWN_NEW_CODE", result.error?.code)
            assertEquals("version", result.error?.details?.single()?.field)
            assertEquals("r1", result.requestId)
            assertEquals("30", result.retryAfter)
        }
        assertEquals(11, server.requestCount)
    }

    @Test fun `malformed missing and oversized error bodies preserve HTTP status`() = fixture { api, server ->
        for (body in listOf("", "<html>error</html>", "{}", "x".repeat(70_000))) {
            server.enqueue(MockResponse().setResponseCode(502).setBody(body))
            val result = networkRequest(json) { api.get() } as NetworkResult.HttpFailure
            assertEquals(502, result.status)
            assertNull(result.error)
        }
    }

    @Test fun `no content succeeds and malformed success is invalid`() = fixture { api, server ->
        server.enqueue(MockResponse().setResponseCode(204))
        assertEquals(NetworkResult.Success<TestPayload>(null,204), networkRequest(json) { api.get() })
        server.enqueue(MockResponse().setBody("not json"))
        assertEquals(NetworkResult.InvalidResponse, networkRequest(json) { api.get() })
    }

    @Test fun `redirect does not replay a mutation`() = fixture { api, server ->
        server.enqueue(MockResponse().setResponseCode(307).setHeader("Location", server.url("/other")))
        assertEquals(307, (networkRequest(json) { api.post() } as NetworkResult.HttpFailure).status)
        assertEquals(1, server.requestCount)
    }

    @Test fun `503 immediate retry does not replay mutation`() = fixture { api, server ->
        server.enqueue(MockResponse().setResponseCode(503).setHeader("Retry-After", "0"))
        server.enqueue(MockResponse().setBody("""{"value":"must not be reached"}"""))
        assertEquals(503, (networkRequest(json) { api.post() } as NetworkResult.HttpFailure).status)
        assertEquals(1, server.requestCount)
    }

    @Test fun `disabled environment fails before network`() = runTest {
        val config = NetworkConfig()
        val client = NetworkClient.httpClient(config)
        val api = NetworkClient.retrofit(config, client, json).create(TestApi::class.java)
        assertEquals(NetworkResult.NotConfigured, networkRequest(json) { api.get() })
        assertFalse(client.retryOnConnectionFailure)
        assertFalse(client.followRedirects)
    }

    @Test fun `transport failures remain distinct and cancellation propagates`() = runTest {
        assertEquals(NetworkResult.Timeout, networkRequest<Unit>(json) { throw SocketTimeoutException() })
        assertEquals(NetworkResult.ConnectionFailure, networkRequest<Unit>(json) { throw IOException() })
        val cancellation = CancellationException("cancelled")
        try {
            networkRequest<Unit>(json) { throw cancellation }
            fail("Cancellation swallowed")
        } catch (actual: CancellationException) { assertSame(cancellation, actual) }
    }

    @Test fun `requests outside base are blocked before IO`() = fixture { api, server ->
        assertEquals(NetworkResult.ConnectionFailure,
            networkRequest(json) { api.dynamic(server.url("/outside").toString()) })
        assertEquals(NetworkResult.ConnectionFailure,
            networkRequest(json) { api.dynamic("https://other.invalid/api/v1/fixture") })
        assertEquals(0, server.requestCount)
    }

    @Test fun `generic envelopes decode metadata and pagination`() {
        val single = json.decodeFromString<ApiResponse<TestPayload>>("""{"data":{"value":"ok"},"meta":{"request_id":"r2"}}""")
        assertEquals("r2", single.meta?.request_id)
        val page = json.decodeFromString<ApiListResponse<TestPayload>>("""{"data":[{"value":"ok"}],"page":{"next_cursor":null,"has_more":false}}""")
        assertEquals("ok", page.data.single().value)
        assertFalse(page.page.has_more)
        assertNull(page.page.next_cursor)
    }

    @Test fun `unsafe base URLs are rejected`() {
        for (url in listOf("http://example.invalid/", "https://user:pass@example.invalid/",
            "https://example.invalid/api", "https://example.invalid/?key=x", "https://example.invalid/#x")) {
            try { NetworkConfig(url.toHttpUrl()); fail(url) } catch (_: IllegalArgumentException) { }
        }
    }
}
