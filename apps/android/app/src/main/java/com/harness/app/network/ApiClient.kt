package com.harness.app.network

import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.cancellation.CancellationException

/** Sends requests and returns the raw response body. Decoding is the job of the network services. */
internal interface ApiClient {
    /** Throws [ApiError]. */
    suspend fun send(endpoint: Endpoint): ByteArray
}

internal class DefaultApiClient(
    private val configuration: ApiConfiguration,
    private val session: HttpSession,
) : ApiClient {

    override suspend fun send(endpoint: Endpoint): ByteArray {
        val request = HttpRequest(makeUrl(endpoint), endpoint.method)
        val response = try {
            session.execute(request)
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            throw ApiError.Transport(error)
        } catch (error: Exception) {
            throw ApiError.Transport(error)
        }
        if (response.statusCode !in 200..299) throw ApiError.HttpStatus(response.statusCode)
        return response.body
    }

    private fun makeUrl(endpoint: Endpoint): URL {
        val query = endpoint.queryItems.joinToString("&") { (name, value) ->
            "${URLEncoder.encode(name, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
        val spec = configuration.baseUrl + endpoint.path + if (query.isEmpty()) "" else "?$query"
        val url = try {
            URI(spec).toURL()
        } catch (error: URISyntaxException) {
            throw ApiError.InvalidUrl
        } catch (error: IllegalArgumentException) {
            throw ApiError.InvalidUrl
        } catch (error: MalformedURLException) {
            throw ApiError.InvalidUrl
        }
        if (url.protocol != "http" && url.protocol != "https") throw ApiError.InvalidUrl
        if (url.port !in -1..65535) throw ApiError.InvalidUrl
        return url
    }
}
