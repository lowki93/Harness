package com.harness.app.network

/** Failures of the request layer. Decoding errors belong to the network services, not here. */
internal sealed class ApiError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data object InvalidUrl : ApiError("Invalid URL")
    data class HttpStatus(val code: Int) : ApiError("HTTP status $code")
    class Transport(cause: Throwable) : ApiError("Transport failure", cause)
}
