package io.karn.instagram.api

import io.karn.instagram.core.Crypto
import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get
import khttp.post
import khttp.responses.Response
import org.json.JSONObject

internal object AuthenticationBootstrapAPI : API() {

    /**
     * No Cookies
     */
    fun getToken(session: Session): Response {
        return get(url = Endpoints.TOKEN_BOOTSTRAP,
                headers = getRequestHeaders(session)
                        // Remove the content type given that its a get request
                        - "Content-Type",
                params = mapOf(
                        "device_id" to session.androidId,
                        "token_hash" to "", // Deliberately empty
                        "custom_device_id" to session.uuid, // alias for X-IG-Device-ID
                        "fetch_reason" to "token_expired"
                ),
                allowRedirects = true
        )
    }

    /**
     * No Cookies
     */
    fun sync(session: Session, data: JSONObject): Response {
        return post(url = Endpoints.LAUNCHER_SYNC,
                headers = getRequestHeaders(session)
                        + ("Host" to "b.i.instagram.com"),
                allowRedirects = true,
                data = Crypto.generateSignedBody(data.toString())
        )
    }

    fun prefillCandidates(session: Session): Response {
        val data = JSONObject()
                .put("android_device_id", session.androidId)
                .put("usages", "[\"account_recovery_omnibox\"]")
                .put("device_id", session.uuid)

        return post(url = Endpoints.PREFILL_CANDIDATES,
                headers = getRequestHeaders(session)
                        + ("Host" to "b.i.instagram.com"),
                allowRedirects = true,
                data = Crypto.generateSignedBody(data.toString())
        )
    }

    fun contactPointPrefill(session: Session): Response {
        val data = JSONObject()
                .put("phone_id", session.phoneId)
                .put("_csrftoken", session.csrfToken)
                .put("usage", "prefill")

        return post(url = Endpoints.CONTACT_POINT_PREFILL,
                headers = getRequestHeaders(session),
                allowRedirects = true,
                data = Crypto.generateSignedBody(data.toString())
        )
    }
}
