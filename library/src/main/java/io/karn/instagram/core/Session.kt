package io.karn.instagram.core

import io.karn.instagram.BuildConfig
import io.karn.instagram.common.generateUUID
import khttp.structures.cookie.CookieJar
import org.json.JSONArray

/**
 * The Session class maintains the Session metadata for the current instance of the library.
 */
data class Session internal constructor(
        internal var instanceId: String = "insights-${BuildConfig.VERSION_NAME}",
        internal var primaryKey: String = "",
        internal var mid: String = "",
        internal var wwwClaim: String = "",
        internal var cookieJar: CookieJar = CookieJar()
) {
    companion object {
        fun buildSession(instanceId: String, primaryKey: String, mid: String, wwwClaim: String, cookies: String): Session {
            return Session(instanceId, primaryKey, mid, wwwClaim, CookieUtils.deserializeFromJson(JSONArray(cookies)))
        }
    }

    val uuid = generateUUID(instanceId)
}
