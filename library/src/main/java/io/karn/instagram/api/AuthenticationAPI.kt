package io.karn.instagram.api

import io.karn.instagram.common.generateUUID
import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get
import khttp.post
import khttp.responses.Response

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

    fun login(session: Session, data: String): Response {
        return post(url = Endpoints.LOGIN,
                headers = getRequestHeaders(session),
                allowRedirects = true,
                data = data)
    }

    fun twoFactor(session: Session, data: String): Response {
        return post(url = Endpoints.LOGIN_APPROVAL,
                headers = getRequestHeaders(session),
                data = data)
    }

    fun prepareAuthChallenge(session: Session, challengePath: String): Response {
        return get(url = String.format(Endpoints.CHALLENGE_PATH, challengePath),
                headers = getRequestHeaders(session),
                params = mapOf(
                        "guid" to session.uuid,
                        "device_id" to session.androidId
                ))
    }

    fun selectAuthChallengeMethod(session: Session, challengePath: String, data: String): Response {
        return post(url = String.format(Endpoints.CHALLENGE_PATH, challengePath),
                headers = getRequestHeaders(session),
                cookies = session.cookieJar,
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
