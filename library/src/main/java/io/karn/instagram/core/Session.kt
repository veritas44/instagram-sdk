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
        internal var claimToken: String = "0",
        internal var authorizationToken: String = "",
        internal var publicKeyId: Int = 209,
        internal var publicKey: String = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUFvSkw5RGQzdWliYmRlOWJVYXlDOQpIMXVJb0RsL3BxeEd3Yjd3dGx6cjRSODhwbGI0SUs1aEdUQ2VTN0xUTXBUNk5oWVFGT2VhajhtcitjVlp1Y1FuCmxQUVNiZTJpM3lIbU9DV2h6L0s0WStzRU1lYmJvZUpuZHpPODFPVVhkUjNZWVN3STJTSFdYTTB0VnhRQjlmZjYKZW0xU3QrSkF6MnhhMDBBMTFod1BraUpIOTdGbU54eWlqL2wrcEdEbXJCQUVLbFNMUzQvdGhGNUNmMEpIVFFwbwpDUkU3VjJDaEtTRlQzNVIvY01TdHR2ekdoQ2dtY1Z5M092aTR5d0VCSkpoTGVrQmV1cG5OWTUvL08rOUxobEhwCmVIcVN1cG9MazZSbDhtTGJkK3ptWTRoWVRzeExDRnpQcDJNSGI1NXZ5eWMxRTdJK1RjcVNXMjFQemlyNWFQcWwKYlFJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==",
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

    internal val rankToken get() = "${dsUserId}_${uuid}"

    internal val dsUserId get() = cookieJar.getCookie("ds_user_id")?.value?.toString() ?: ""
    internal val csrfToken get() = cookieJar.getCookie("csrftoken")?.value?.toString() ?: ""
    internal val midToken get() = cookieJar.getCookie("mid")?.value?.toString() ?: ""

    fun serialize(): String {
        val data = JSONObject()
                .put("claimToken", claimToken)
                .put("authorizationToken", authorizationToken)
                .put("cookies", CookieUtils.serializeToJson(cookieJar))

        return data.toString()
    }
}
