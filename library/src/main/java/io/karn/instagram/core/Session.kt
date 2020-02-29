package io.karn.instagram.core

import io.karn.instagram.BuildConfig
import io.karn.instagram.Instagram
import io.karn.instagram.common.CookieUtils
import io.karn.instagram.common.CryptoUtils
import io.karn.instagram.common.generateUUID
import khttp.structures.cookie.CookieJar
import org.json.JSONObject

/**
 * The Session class maintains the Session metadata for the current instance of the library.
 */
data class Session internal constructor(
        internal var instanceId: String = "insights-${BuildConfig.VERSION_NAME}",
        internal var primaryKey: String = "",
        internal var csrfToken: String = "",
        internal var midToken: String = "",
        internal var claimToken: String = "",
        internal var authorizationToken: String = "",
        internal var publicKeyId: Int = 205,
        internal var publicKey: String = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF1enRZOEZvUlRGRU9mK1RkTGlUdAplN3FIQXY1cmdBMmk5RkQ0YjgzZk1GK3hheW14b0xSdU5KTitRanJ3dnBuSm1LQ0QxNGd3K2w3TGQ0RHkvRHVFCkRiZlpKcmRRWkJIT3drS3RqdDdkNWlhZFdOSjdLczlBM0NNbzB5UktyZFBGU1dsS21lQVJsTlFrVXF0YkNmTzcKT2phY3ZYV2dJcGlqTkdJRVk4UkdzRWJWZmdxSmsrZzhuQWZiT0xjNmEwbTMxckJWZUJ6Z0hkYWExeFNKOGJHcQplbG4zbWh4WDU2cmpTOG5LZGk4MzRZSlNaV3VxUHZmWWUrbEV6Nk5laU1FMEo3dE80eWxmeWlPQ05ycnF3SnJnCjBXWTFEeDd4MHlZajdrN1NkUWVLVUVaZ3FjNUFuVitjNUQ2SjJTSTlGMnNoZWxGNWVvZjJOYkl2TmFNakpSRDgKb1FJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==",
        internal var cookieJar: CookieJar = CookieJar()
) {
    internal val deviceDPI get() = Instagram.instance.configuration.deviceDPI
    internal val deviceResolution get() = Instagram.instance.configuration.deviceResolution

    internal val uuid = generateUUID(instanceId)

    internal val androidId get() = CryptoUtils.generateAndroidId(instanceId)

    internal val adid get() = generateUUID(instanceId + "_adid")

    internal val phoneId get() = generateUUID(instanceId + "_phone")
    internal val jazoest get() = CryptoUtils.createJazoest(phoneId)

    internal val pigeonSessionId = Crypto.generateTemporaryGUID("pigeonSessionId", uuid, 1200000f)

    internal val rankToken get() = "${primaryKey}_${uuid}"

    fun serialize(): String {
        val data = JSONObject()
                .put("primaryKey", primaryKey)
                .put("csrfToken", csrfToken)
                .put("midToken", midToken)
                .put("claimToken", claimToken)
                .put("authorizationToken", authorizationToken)
                .put("cookies", CookieUtils.serializeToJson(cookieJar))

        return data.toString()
    }
}
