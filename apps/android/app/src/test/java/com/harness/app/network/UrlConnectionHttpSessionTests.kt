package com.harness.app.network

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
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

/** Wraps a real connection and records what the session configured and whether it disconnected. */
private class RecordingConnection(url: URL, private val real: HttpURLConnection) : HttpURLConnection(url) {
    var disconnectCount = 0
        private set

    override fun setRequestMethod(method: String) {
        real.requestMethod = method
        super.setRequestMethod(method)
    }

    override fun setConnectTimeout(timeout: Int) {
        real.connectTimeout = timeout
        super.setConnectTimeout(timeout)
    }

    override fun setReadTimeout(timeout: Int) {
        real.readTimeout = timeout
        super.setReadTimeout(timeout)
    }

    override fun getResponseCode(): Int = real.responseCode

    override fun getInputStream(): InputStream = real.inputStream

    override fun getErrorStream(): InputStream? = real.errorStream

    override fun connect() = real.connect()

    override fun usingProxy(): Boolean = real.usingProxy()

    override fun disconnect() {
        disconnectCount++
        real.disconnect()
    }
}

private class RecordingHandler : URLStreamHandler() {
    var connection: RecordingConnection? = null

    override fun openConnection(url: URL): URLConnection {
        val real = URL(url.toString()).openConnection() as HttpURLConnection
        return RecordingConnection(url, real).also { connection = it }
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
        private val handler = RecordingHandler()

        private suspend fun executeDefaultSession(): RecordingConnection {
            val local = serve { it.reply(200) }
            val url = URL(null, local.url().toString(), handler)
            UrlConnectionHttpSession(Dispatchers.IO).execute(HttpRequest(url, HttpMethod.GET))
            return handler.connection!!
        }

        @Test
        fun givenDefaultSession_whenExecuting_thenConnectTimeoutIsTenSeconds() = runTest {
            assertEquals(10_000, executeDefaultSession().connectTimeout)
        }

        @Test
        fun givenDefaultSession_whenExecuting_thenReadTimeoutIsTenSeconds() = runTest {
            assertEquals(10_000, executeDefaultSession().readTimeout)
        }

        @Test
        fun givenAnyResponse_whenExecuting_thenDisconnects() = runTest {
            assertEquals(1, executeDefaultSession().disconnectCount)
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
