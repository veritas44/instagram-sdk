package io.karn.instagram.core

import io.karn.instagram.common.CryptoUtils
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.roundToInt

internal object Crypto {
    private const val SIG_KEY = "9193488027538fd3450b83b7d05286d4ca9599a0f7eeed90d8c85925698a05dc"
    private const val SIG_VERSION = "4"


    internal fun generateTemporaryGUID(name: String, uuid: String, duration: Float): String {
        return UUID.nameUUIDFromBytes("$name$uuid${(System.currentTimeMillis() / duration).roundToInt()}".toByteArray()).toString()
    }

    fun generateLoginPayload(session: Session, token: String, username: String, password: String, loginAttempts: Int): String {
        val time = (System.currentTimeMillis() / 1000).toString()

        // TODO: Validate the public key and ID.

        val encPassword = CryptoUtils.encryptPassword(session.publicKey, session.publicKeyId, time, password)

        val data = JSONObject()
                .put("username", username)
                .put("password", password)
                .put("enc_password", "#PWD_INSTAGRAM:4:$time:$encPassword")
                .put("guid", session.uuid)
                .put("phone_id", session.phoneId)
                .put("_csrftoken", token)
                .put("device_id", session.androidId)
                .put("adid", session.adid)
                .put("google_tokens", "[]")
                .put("login_attempt_count", loginAttempts)
                // TODO: Adjust this to have the correct country code
                .put("country_codes", """[{"country_code":"1","source":["default"]}]""")
                .put("jazoest", session.jazoest)

        return generateSignature(data.toString())
    }

    fun generateTwoFactorPayload(session: Session, code: String, identifier: String, token: String, username: String, password: String): String {
        val data = JSONObject()
                .put("verification_code", code)
                .put("two_factor_identifier", identifier)
                .put("_csrftoken", token)
                .put("username", username)
                .put("device_id", session.androidId)
                .put("password", password)

        return generateSignature(data.toString())
    }

    fun generateAuthenticatedParams(session: Session, mutate: (JSONObject) -> Unit = {}): String {
        val data = JSONObject()
                .put("_uuid", session.uuid)
                .put("_uid", session.primaryKey)
                .put("_csrftoken", session.csrfToken)

        mutate(data)

        return generateSignature(data.toString())
    }

    fun generateAuthenticatedChallengeParams(session: Session, mutate: (JSONObject) -> Unit = {}): String {
        val data = JSONObject()
                .put("guid", session.uuid)
                .put("device_id", session.androidId)
                .put("_csrftoken", session.csrfToken)

        mutate(data)

        return generateSignature(data.toString())
    }

    private fun generateSignedBody(key: String, data: String): String {
        val sha256HMAC = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(Charset.forName("UTF-8")), "HmacSHA256")
        sha256HMAC.init(secretKey)

        return CryptoUtils.bytesToHex(sha256HMAC.doFinal(data.toByteArray(Charset.forName("UTF-8")))).toLowerCase()
    }

    internal fun generateSignature(payload: String): String {
        val parsedData = URLEncoder.encode(payload, "UTF-8")

        val signedBody = generateSignedBody(SIG_KEY, payload)

        return ("signed_body=$signedBody.$parsedData&ig_sig_key_version=$SIG_VERSION")
    }
}
