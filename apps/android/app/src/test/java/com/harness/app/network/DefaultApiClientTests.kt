package com.harness.app.network

import com.harness.app.mock.network.HttpSessionMock
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

private const val BASE_URL = "http://10.0.2.2:3000"

private class TestEndpoint(
    override val path: String,
    override val method: HttpMethod = HttpMethod.GET,
    override val queryItems: List<Pair<String, String>> = emptyList(),
) : Endpoint

@RunWith(Enclosed::class)
class DefaultApiClientTests {

    class Success {
        private val body = """{"status":"ok"}""".toByteArray()
        private val session = HttpSessionMock(statusCode = 200, body = body)
        private val client = DefaultApiClient(ApiConfiguration(BASE_URL), session)

        @Test
        fun givenSuccessResponse_whenSending_thenReturnsExactBody() = runTest {
            val received = client.send(TestEndpoint("/health"))

            assertArrayEquals(body, received)
        }

        @Test
        fun givenAnyStatusIn2xx_whenSending_thenReturnsBody() = runTest {
            val created = DefaultApiClient(ApiConfiguration(BASE_URL), HttpSessionMock(statusCode = 201, body = body))

            assertArrayEquals(body, created.send(TestEndpoint("/items")))
        }
    }

    class RequestBuilding {
        private val session = HttpSessionMock(statusCode = 200)
        private val client = DefaultApiClient(ApiConfiguration(BASE_URL), session)

        @Test
        fun givenPath_whenSending_thenUrlIsBaseUrlPlusPath() = runTest {
            client.send(TestEndpoint("/health"))

            assertEquals("http://10.0.2.2:3000/health", session.requests.single().url.toString())
        }

        @Test
        fun givenQueryItems_whenSending_thenUrlContainsEncodedQuery() = runTest {
            client.send(TestEndpoint("/search", queryItems = listOf("q" to "a b", "page" to "2")))

            assertEquals("http://10.0.2.2:3000/search?q=a+b&page=2", session.requests.single().url.toString())
        }

        @Test
        fun givenNoQueryItems_whenSending_thenUrlHasNoQuery() = runTest {
            client.send(TestEndpoint("/health"))

            assertEquals(null, session.requests.single().url.query)
        }

        @Test
        fun givenDefaultMethod_whenSending_thenMethodIsGet() = runTest {
            client.send(TestEndpoint("/health"))

            assertEquals(HttpMethod.GET, session.requests.single().method)
        }

        @Test
        fun givenPostEndpoint_whenSending_thenMethodIsPost() = runTest {
            client.send(TestEndpoint("/items", method = HttpMethod.POST))

            assertEquals(HttpMethod.POST, session.requests.single().method)
        }
    }

    class HttpStatusFailure {
        @Test
        fun givenNon2xxResponse_whenSending_thenThrowsHttpStatusWithCode() = runTest {
            val client = DefaultApiClient(ApiConfiguration(BASE_URL), HttpSessionMock(statusCode = 503))

            val error = runCatching { client.send(TestEndpoint("/health")) }.exceptionOrNull()

            assertEquals(ApiError.HttpStatus(503), error)
        }

        @Test
        fun givenRedirectStatus_whenSending_thenThrowsHttpStatus() = runTest {
            val client = DefaultApiClient(ApiConfiguration(BASE_URL), HttpSessionMock(statusCode = 302))

            val error = runCatching { client.send(TestEndpoint("/health")) }.exceptionOrNull()

            assertEquals(ApiError.HttpStatus(302), error)
        }
    }

    class InvalidUrlFailure {
        @Test
        fun givenMalformedBaseUrl_whenSending_thenThrowsInvalidUrl() = runTest {
            val session = HttpSessionMock(statusCode = 200)
            val client = DefaultApiClient(ApiConfiguration("not a url"), session)

            val error = runCatching { client.send(TestEndpoint("/health")) }.exceptionOrNull()

            assertEquals(ApiError.InvalidUrl, error)
        }

        @Test
        fun givenMalformedBaseUrl_whenSending_thenSessionIsNeverCalled() = runTest {
            val session = HttpSessionMock(statusCode = 200)
            val client = DefaultApiClient(ApiConfiguration("not a url"), session)

            runCatching { client.send(TestEndpoint("/health")) }

            assertEquals(0, session.requests.size)
        }

        @Test
        fun givenPortOutOfRange_whenSending_thenThrowsInvalidUrl() = runTest {
            val session = HttpSessionMock(statusCode = 200)
            val client = DefaultApiClient(ApiConfiguration("http://127.0.0.1:99999"), session)

            val error = runCatching { client.send(TestEndpoint("/health")) }.exceptionOrNull()

            assertEquals(ApiError.InvalidUrl, error)
            assertEquals(0, session.requests.size)
        }

        @Test
        fun givenNonHttpScheme_whenSending_thenThrowsInvalidUrl() = runTest {
            val session = HttpSessionMock(statusCode = 200)
            val client = DefaultApiClient(ApiConfiguration("ftp://127.0.0.1"), session)

            val error = runCatching { client.send(TestEndpoint("/health")) }.exceptionOrNull()

            assertEquals(ApiError.InvalidUrl, error)
            assertEquals(0, session.requests.size)
        }

        @Test
        fun givenSpaceInPath_whenSending_thenThrowsInvalidUrl() = runTest {
            val session = HttpSessionMock(statusCode = 200)
            val client = DefaultApiClient(ApiConfiguration(BASE_URL), session)

            val error = runCatching { client.send(TestEndpoint("/ec ho")) }.exceptionOrNull()

            assertEquals(ApiError.InvalidUrl, error)
            assertEquals(0, session.requests.size)
        }
    }

    class TransportFailure {
        @Test
        fun givenIoException_whenSending_thenThrowsTransportWithCause() = runTest {
            val cause = IOException("connection reset")
            val client = DefaultApiClient(ApiConfiguration(BASE_URL), HttpSessionMock(Result.failure(cause)))

            val error = runCatching { client.send(TestEndpoint("/health")) }.exceptionOrNull()

            assertEquals(cause, (error as ApiError.Transport).cause)
        }

        @Test
        fun givenSocketTimeout_whenSending_thenThrowsTransport() = runTest {
            val client = DefaultApiClient(
                ApiConfiguration(BASE_URL),
                HttpSessionMock(Result.failure(SocketTimeoutException("timeout"))),
            )

            val error = runCatching { client.send(TestEndpoint("/health")) }.exceptionOrNull()

            assertEquals(true, error is ApiError.Transport)
        }

        @Test
        fun givenNonIoException_whenSending_thenThrowsTransport() = runTest {
            val cause = IllegalStateException("boom")
            val client = DefaultApiClient(ApiConfiguration(BASE_URL), HttpSessionMock(Result.failure(cause)))

            val error = runCatching { client.send(TestEndpoint("/health")) }.exceptionOrNull()

            assertEquals(cause, (error as ApiError.Transport).cause)
        }
    }
}
