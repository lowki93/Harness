package com.harness.app.network

import java.net.HttpURLConnection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal const val TIMEOUT_MILLIS = 10_000

/** Blocking `HttpURLConnection` work runs on the injected [dispatcher] (`Dispatchers.IO` in production). */
internal class UrlConnectionHttpSession(
    private val dispatcher: CoroutineDispatcher,
) : HttpSession {

    override suspend fun execute(request: HttpRequest): HttpResponse = withContext(dispatcher) {
        val connection = request.url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = request.method.name
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            HttpResponse(statusCode, stream?.use { it.readBytes() } ?: ByteArray(0))
        } finally {
            connection.disconnect()
        }
    }
}
