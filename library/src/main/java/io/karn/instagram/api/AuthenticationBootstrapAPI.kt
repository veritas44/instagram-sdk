package io.karn.instagram.api

import io.karn.instagram.core.Crypto
import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.post
import khttp.responses.Response
import org.json.JSONObject

internal object AuthenticationBootstrapAPI : API() {

    fun getHeaders(session: Session, data: JSONObject): Response {
        return post(url = Endpoints.MSISDN_HEADER_BOOTSTRAP,
                headers = getRequestHeaders(session, false) + ("Host" to "b.i.instagram.com"),
                allowRedirects = true,
                data = Crypto.generateSignature(data.toString())
        )
    }

    fun getSync(session: Session, data: JSONObject): Response {
        return post(url = Endpoints.LAUNCHER_SYNC,
                headers = getRequestHeaders(session, false) +
                        ("Host" to "b.i.instagram.com") +
                        ("X-DEVICE-ID" to session.uuid),
                allowRedirects = true,
                data = Crypto.generateSignature(data.toString())
        )
    }
}
