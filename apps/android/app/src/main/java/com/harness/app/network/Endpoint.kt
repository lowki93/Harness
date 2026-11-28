package com.harness.app.network

/** Describes one API call. Declare one sealed class or enum per resource that implements it. */
internal interface Endpoint {
    val path: String
    val method: HttpMethod get() = HttpMethod.GET
    val queryItems: List<Pair<String, String>> get() = emptyList()
}
