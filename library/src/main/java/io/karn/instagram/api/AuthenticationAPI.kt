package io.karn.instagram.api

import io.karn.instagram.common.generateUUID
import io.karn.instagram.core.Crypto
import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get
import khttp.post
import khttp.responses.Response
import org.json.JSONObject

internal object AuthenticationAPI : API() {

    fun getTokenForAuth(session: Session): Response {
        return get(url = Endpoints.CSRF_TOKEN,
                params = mapOf(
                        "challenge_type" to "signup",
                        "guid" to generateUUID(session.instanceId)
                ),
                headers = getRequestHeaders(session),
                allowRedirects = true)
    }

    fun login(session: Session, data: JSONObject): Response {
        return post(url = Endpoints.LOGIN,
                headers = getRequestHeaders(session)
                        + ("Host" to "i.instagram.com"),
                allowRedirects = true,
                data = Crypto.generateSignedBody(data.toString()))
    }

    fun twoFactor(session: Session, data: String): Response {
        return post(url = Endpoints.LOGIN_APPROVAL,
                headers = getRequestHeaders(session),
                data = data)
    }

    fun prepareAuthChallenge(session: Session, challengePath: String, nonce: String, userId: String): Response {
        return get(url = String.format(Endpoints.CHALLENGE_PATH, challengePath),
                headers = getRequestHeaders(session),
                params = mapOf(
                        "guid" to session.uuid,
                        "device_id" to session.androidId,
                        "challenge_context" to """{"step_name": "", "nonce_code": "$nonce", "user_id": $userId, "is_stateless": false}"""
                ))
    }

    fun selectAuthChallengeMethod(session: Session, challengePath: String, data: String): Response {
        return post(url = String.format(Endpoints.CHALLENGE_PATH, challengePath),
                headers = getRequestHeaders(session),
                data = data)
    }

    fun submitAuthChallenge(session: Session, challengePath: String, data: String): Response {
        return post(url = String.format(Endpoints.CHALLENGE_PATH, challengePath),
                headers = getRequestHeaders(session),
                cookies = session.cookieJar,
                data = data)
    }

    fun logout(session: Session): Response {
        return post(url = Endpoints.LOGOUT,
                headers = getRequestHeaders(session),
                data = hashMapOf("guid" to session.uuid))
    }
}
