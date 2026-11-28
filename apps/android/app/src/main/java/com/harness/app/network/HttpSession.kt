package com.harness.app.network

import java.net.URL

internal data class HttpRequest(val url: URL, val method: HttpMethod)

/** [body] is the response body, or the error body on a non-2xx status. */
internal class HttpResponse(val statusCode: Int, val body: ByteArray)

/** Seam over `HttpURLConnection` so the API client can be tested without real requests. */
internal interface HttpSession {
    /** Throws `IOException` (including `SocketTimeoutException`) on transport failure. */
    suspend fun execute(request: HttpRequest): HttpResponse
}
