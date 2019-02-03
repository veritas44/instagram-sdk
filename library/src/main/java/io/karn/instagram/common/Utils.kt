package io.karn.instagram.common

import io.karn.instagram.exceptions.InstagramAPIException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Wraps an execution in a collection of Exception handlers which map the exception out to a single
 * {@link InstagramAPIException} object.
 */
internal fun <T> wrapAPIException(block: () -> T): T {
    try {
        return block()
    } catch (socketTimeout: SocketTimeoutException) {
        throw InstagramAPIException(408, "API request timed out.", socketTimeout)
    } catch (sslException: SSLException) {
        throw InstagramAPIException(408, "Unable to create connection", sslException)
    } catch (connectException: ConnectException) {
        throw InstagramAPIException(408, "Unable to create connection.", connectException)
    } catch (unknownHostException: UnknownHostException) {
        throw InstagramAPIException(408, "Unable to connect to host.", unknownHostException)
    }
}