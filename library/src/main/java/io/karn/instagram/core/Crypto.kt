package io.karn.instagram.core

import io.karn.instagram.Instagram
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.HashMap
import java.util.Locale
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.roundToInt

internal object Crypto {
    private const val SIG_KEY = "a86109795736d73c9a94172cd9b736917d7d94ca61c9101164894b3f0d43bef4"
    private const val SIG_VERSION = "4"

    internal const val DPI: String = "640dpi"
    internal const val DISPLAY_RESOLUTION: String = "1440x2560"

    private const val APP_ID = "567067343352427"
    private const val APP_VERSION = "117.0.0.28.123"
    private const val VERSION_CODE: String = "180322800"
    private const val BLOKS_VERSION_ID: String = "0a3ae4c88248863609c67e278f34af44673cff300bc76add965a9fb036bd3ca3"

    private val PIGEON_SESSION_ID = generateTemporaryGUID("pigeonSessionId", Instagram.session.uuid, 1200000f)

    val HEADERS: HashMap<String, String>
        get() = hashMapOf(
                "Accept-Encoding" to "gzip, deflate",
                "Connection" to "keep-alive",
                "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
                "Host" to "i.instagram.com",
                "X-IG-App-Locale" to getFormattedLocale(Locale.getDefault()),
                "X-IG-Device-Locale" to getFormattedLocale(Locale.getDefault()),
                "X-Pigeon-Session-Id" to PIGEON_SESSION_ID,
                "X-Pigeon-Rawclienttime" to "%.3f".format(System.currentTimeMillis() / 1000f),
                "X-IG-Connection-Speed" to "-1kbps",
                "X-IG-Bandwidth-Speed-KBPS" to "-1.000",
                "X-IG-Bandwidth-TotalBytes-B" to "0",
                "X-IG-Bandwidth-TotalTime-MS" to "0",
                "X-Bloks-Version-Id" to BLOKS_VERSION_ID,
                "X-IG-WWW-Claim" to Instagram.session.wwwClaim,
                "X-Bloks-Is-Layout-RTL" to "false",
                "X-Bloks-Enable-RenderCore" to "false",
                "X-IG-Device-ID" to generateUUID(Instagram.config.instanceId),
                "X-IG-Android-ID" to generateAndroidId(Instagram.config.instanceId),
                "X-IG-Connection-Type" to "WIFI",
                "X-IG-Capabilities" to "3brTvwE=",
                "X-IG-App-ID" to APP_ID,
                "User-Agent" to buildUserAgent(),
                "Accept-Language" to getFormattedLocale(Locale.getDefault(), "-"),
                "X-FB-HTTP-Engine" to "Liger"
        )


    /**
     * Function to build the UserAgent which is used with the API to manage user authentication. This User Agent must be
     * correct otherwise the authentication step will fail.
     *
     * The User Agent's defaults are set below in the event that this function is exposed in the future. The parameters
     * that are known to work are as follows.
     *
     *  androidVersion = "24"
     *  androidRelease = "7.0"
     *  dpi = "640dpi"
     *  resolution = "1440x2560"
     *  manufacturer = "samsung"
     *  brand = ""
     *  device = "herolte"
     *  model = "SM-G930F"
     *  hardware = "samsungexynos8890"
     */
    private fun buildUserAgent(androidVersion: Int = android.os.Build.VERSION.SDK_INT,
                               androidRelease: String = android.os.Build.VERSION.RELEASE,
                               dpi: String = Instagram.config.deviceDPI,
                               resolution: String = Instagram.config.deviceResolution,
                               manufacturer: String = android.os.Build.MANUFACTURER,
                               brand: String = android.os.Build.BRAND.takeIf { !it.isNullOrBlank() }?.let { "/$it" } ?: "",
                               device: String = android.os.Build.DEVICE,
                               model: String = android.os.Build.MODEL,
                               hardware: String = android.os.Build.HARDWARE): String {

        return "Instagram $APP_VERSION Android ($androidVersion/$androidRelease; $dpi; $resolution; $manufacturer$brand; $model; $device; $hardware; en_US; $VERSION_CODE)"
    }

    private fun generateTemporaryGUID(name: String, uuid: String, duration: Float): String {
        return UUID.nameUUIDFromBytes("$name$uuid${(System.currentTimeMillis() / duration).roundToInt()}".toByteArray()).toString()
    }

    fun generateLoginPayload(token: String, username: String, password: String, loginAttempts: Int, deviceId: String = generateAndroidId(Instagram.config.instanceId)): String {
        val data = JSONObject()
                .put("phone_id", generateUUID(Instagram.config.instanceId + "_phone_id"))
                .put("_csrftoken", token)
                .put("username", username)
                .put("adid", generateUUID(Instagram.config.instanceId + "_adid"))
                .put("guid", Instagram.session.uuid)
                .put("device_id", deviceId)
                .put("google_tokens", "[]")
                .put("password", password)
                .put("login_attempt_count", loginAttempts)
        // .put("enc_password", "#PWD_INSTAGRAM:4:1579482891:Ac3AOYGboYYL98jh0IoAAWLn1zIcafA0Sji0/Yz2dUCE2wUE3GFqzRHxEcT7N+YLr2KtM0dOQajExheNMpUxA4cQ3Fi/J7L7J88tOoNczBblmQTD3Uj11+Ik2b6vPB6gHlTmJvt2bCxHfj4Z5IP8+fzoW8zGYp89pY2FHuj6SQaKaEtM+e2LYvJDfXs5/5tdQOAe7GdStbdbYi1XOFzjJbWeDuvms5cUOXeaW9pUutioODcco6O239WLQdKAmL4kBelBymFautvKrI1vf0cFWgd9urmFeL48eKFUFeyyrzUyo22zh+ddB3GI38+hR5nbmVeZ80/53h8L+h98GzezErqfHsR1US23blU8i+SnbVuUtzPE//DZZ2oObiGaKNKAZbvHAdkleg")

        return generateSignature(data.toString())
    }

    fun generateTwoFactorPayload(code: String, identifier: String, token: String, username: String, password: String, deviceId: String = generateAndroidId(Instagram.config.instanceId)): String {
        val data = JSONObject()
                .put("verification_code", code)
                .put("two_factor_identifier", identifier)
                .put("_csrftoken", token)
                .put("username", username)
                .put("device_id", deviceId)
                .put("password", password)

        return generateSignature(data.toString())
    }

    fun generateAuthenticatedParams(session: Session, mutate: (JSONObject) -> Unit = {}): String {
        val data = JSONObject()
                .put("_uuid", session.uuid)
                .put("_uid", session.primaryKey)
                .put("_csrftoken", session.cookieJar.getCookie("csrftoken")?.value?.toString()
                        ?: "")

        mutate(data)

        return generateSignature(data.toString())
    }

    fun generateAuthenticatedChallengeParams(session: Session, mutate: (JSONObject) -> Unit = {}): String {
        val data = JSONObject()
                .put("guid", session.uuid)
                .put("device_id", session.androidId)
                .put("_csrftoken", session.cookieJar.getCookie("csrftoken")?.value?.toString()
                        ?: "")

        mutate(data)

        return generateSignature(data.toString())
    }

    fun generateAndroidId(instanceId: String): String {
        val seed = md5Hex(instanceId)
        val volatileSeed = "12345"

        return "android-" + md5Hex(seed + volatileSeed).substring(0, 16)
    }

    fun generateUUID(instanceId: String): String {
        return UUID.nameUUIDFromBytes(instanceId.toByteArray()).toString()
    }

    fun generateSignature(payload: String): String {
        val parsedData = URLEncoder.encode(payload, "UTF-8")

        val signedBody = generateSignedBody(SIG_KEY, payload)

        return ("signed_body=$signedBody.$parsedData&ig_sig_key_version=$SIG_VERSION")
    }

    private fun digest(codec: String, source: String): String {
        val digest = MessageDigest.getInstance(codec)
        val digestBytes = digest.digest(source.toByteArray(Charset.forName("UTF-8")))

        return bytesToHex(digestBytes)
    }

    private fun md5Hex(source: String): String = digest("MD5", source)

    private fun bytesToHex(bytes: ByteArray): String {
        val builder = StringBuilder()

        bytes.forEach { builder.append(String.format("%02x", it)) }

        return builder.toString()
    }

    private fun generateSignedBody(key: String, data: String): String {
        val sha256HMAC = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(Charset.forName("UTF-8")), "HmacSHA256")
        sha256HMAC.init(secretKey)

        return bytesToHex(sha256HMAC.doFinal(data.toByteArray(Charset.forName("UTF-8")))).toLowerCase()
    }

    private fun getFormattedLocale(locale: Locale, separator: String = "_"): String {
        return "${locale.language}${separator}${locale.country}"
    }
}
