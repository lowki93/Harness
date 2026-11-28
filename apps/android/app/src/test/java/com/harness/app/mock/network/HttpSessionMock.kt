package com.harness.app.mock.network

import com.harness.app.network.HttpRequest
import com.harness.app.network.HttpResponse
import com.harness.app.network.HttpSession

/** Records every request and replays a fixed [Result], so the API client is tested without a network. */
internal class HttpSessionMock(private val result: Result<HttpResponse>) : HttpSession {
    private val recordedRequests = mutableListOf<HttpRequest>()
    val requests: List<HttpRequest> get() = recordedRequests

    constructor(statusCode: Int, body: ByteArray = ByteArray(0)) :
        this(Result.success(HttpResponse(statusCode, body)))

    override suspend fun execute(request: HttpRequest): HttpResponse {
        recordedRequests += request
        return result.getOrThrow()
    }
}
