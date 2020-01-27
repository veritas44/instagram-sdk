package io.karn.instagram.common

import io.karn.instagram.Instagram
import io.karn.instagram.exceptions.InstagramAPIException
import khttp.responses.Response
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.ArrayDeque
import java.util.Deque
import java.util.UUID
import javax.net.ssl.SSLException

/**
 * Wraps an execution in a collection of Exception handlers which map the exception out to a single
 * {@link InstagramAPIException} object.
 */
internal fun <T : Response> wrapAPIException(block: () -> T): Pair<T?, InstagramAPIException?> {
    val error = try {
        val response = block()

        // Ensure that the response is of type JSON before proceeding.
        if (!response.headers.any { it.value.contains("application/json") }) {
            return Pair(null, InstagramAPIException(response.statusCode, "Unable to parse JSON response."))
        }

        if (response.headers.containsKey("x-ig-set-www-claim")) {
            Instagram.session.wwwClaim = response.headers["x-ig-set-www-claim"] ?: "0"
        }

        if (response.headers.containsKey("ig-set-x-mid")) {
            Instagram.session.mid = response.headers["ig-set-x-mid"] ?: ""
        }

        return Pair(response, null)
    } catch (socketTimeout: SocketTimeoutException) {
        InstagramAPIException(408, "API request timed out.", socketTimeout)
    } catch (sslException: SSLException) {
        InstagramAPIException(408, "Unable to create connection.", sslException)
    } catch (connectException: ConnectException) {
        InstagramAPIException(408, "Unable to create connection.", connectException)
    } catch (unknownHostException: UnknownHostException) {
        InstagramAPIException(408, "Unable to connect to host.", unknownHostException)
    }

    return Pair(null, error)
}

fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
    return JsonObjectBuilder().json(build)
}

class JsonObjectBuilder {
    private val deque: Deque<JSONObject> = ArrayDeque()

    fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
        deque.push(JSONObject())
        this.build()
        return deque.pop()
    }

    infix fun <T> String.to(value: T) {
        deque.peek().put(this, value)
    }
}

fun generateUUID(seed: String): String {
    return UUID.nameUUIDFromBytes(seed.toByteArray()).toString()
}

/**
 * Indicates the feature is in experimental state: its existence, signature or behavior
 * might change without warning from one release to the next.
 */
annotation class Experimental
