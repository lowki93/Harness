package com.harness.app.network

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

/** A local JDK server on a free port, so the session is exercised through a real `HttpURLConnection`. */
class LocalServer(handler: (HttpExchange) -> Unit) {
    private val server = HttpServer.create(InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0).apply {
        createContext("/") { exchange ->
            try {
                handler(exchange)
            } finally {
                exchange.close()
            }
        }
        start()
    }

    fun url(path: String = "/"): URL = URL("http://127.0.0.1:${server.address.port}$path")

    fun stop() = server.stop(0)
}

private fun HttpExchange.reply(status: Int, body: ByteArray = ByteArray(0)) {
    sendResponseHeaders(status, if (body.isEmpty()) -1 else body.size.toLong())
    if (body.isNotEmpty()) responseBody.write(body)
}

class CountingDispatcher : CoroutineDispatcher() {
    var dispatchCount = 0
        private set

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatchCount++
        block.run()
    }
}

abstract class ServerTest {
    protected var server: LocalServer? = null

    protected fun serve(handler: (HttpExchange) -> Unit): LocalServer = LocalServer(handler).also { server = it }

    @After
    fun tearDown() {
        server?.stop()
    }
}

@RunWith(Enclosed::class)
class UrlConnectionHttpSessionTests {

    class Responses : ServerTest() {
        private val session = UrlConnectionHttpSession(Dispatchers.IO)

        @Test
        fun given2xxResponse_whenExecuting_thenReturnsStatusAndBody() = runTest {
            val body = """{"status":"ok"}""".toByteArray()
            val local = serve { it.reply(200, body) }

            val response = session.execute(HttpRequest(local.url("/health"), HttpMethod.GET))

            assertEquals(200, response.statusCode)
            assertArrayEquals(body, response.body)
        }

        @Test
        fun givenNon2xxResponse_whenExecuting_thenReturnsStatusAndErrorStreamBody() = runTest {
            val body = """{"error":"not found"}""".toByteArray()
            val local = serve { it.reply(404, body) }

            val response = session.execute(HttpRequest(local.url("/missing"), HttpMethod.GET))

            assertEquals(404, response.statusCode)
            assertArrayEquals(body, response.body)
        }

        @Test
        fun givenNon2xxResponseWithoutBody_whenExecuting_thenReturnsEmptyBody() = runTest {
            val local = serve { it.reply(500) }

            val response = session.execute(HttpRequest(local.url(), HttpMethod.GET))

            assertEquals(500, response.statusCode)
            assertEquals(0, response.body.size)
        }

        @Test
        fun givenPostRequest_whenExecuting_thenServerReceivesPost() = runTest {
            var received: String? = null
            val local = serve {
                received = it.requestMethod
                it.reply(200)
            }

            session.execute(HttpRequest(local.url(), HttpMethod.POST))

            assertEquals("POST", received)
        }
    }

    class Configuration : ServerTest() {
        @Test
        fun givenTimeoutConstant_whenInspecting_thenIsTenSeconds() {
            assertEquals(10_000, TIMEOUT_MILLIS)
        }

        @Test
        fun givenInjectedDispatcher_whenExecuting_thenRunsOnIt() = runTest {
            val dispatcher = CountingDispatcher()
            val local = serve { it.reply(200) }

            UrlConnectionHttpSession(dispatcher).execute(HttpRequest(local.url(), HttpMethod.GET))

            assertTrue(dispatcher.dispatchCount > 0)
        }
    }

    class Failures : ServerTest() {
        @Test
        fun givenServerSlowerThanReadTimeout_whenExecuting_thenThrowsSocketTimeout() = runTest {
            val local = serve {
                Thread.sleep(1_000)
                it.reply(200)
            }
            val session = UrlConnectionHttpSession(Dispatchers.IO, timeoutMillis = 100)

            val error = runCatching { session.execute(HttpRequest(local.url(), HttpMethod.GET)) }.exceptionOrNull()

            assertTrue(error is SocketTimeoutException)
        }

        @Test
        fun givenUnreachableServer_whenExecuting_thenIoExceptionPropagates() = runTest {
            val local = serve { it.reply(200) }
            val url = local.url()
            local.stop()

            val error = runCatching {
                UrlConnectionHttpSession(Dispatchers.IO).execute(HttpRequest(url, HttpMethod.GET))
            }.exceptionOrNull()

            assertTrue(error is IOException)
        }
    }
}
