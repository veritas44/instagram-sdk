package io.karn.instagram.api

import io.karn.instagram.Instagram
import io.karn.instagram.common.CookieUtils
import io.karn.instagram.core.Session
import java.util.Locale

open class API {

    companion object {
        private const val APP_VERSION = "121.0.0.29.119"
        private const val VERSION_CODE = "185203708"

        private const val FACEBOOK_ANALYTICS_ID = "567067343352427"

        private const val BLOKS_VERSION_ID = "1b030ce63a06c25f3e4de6aaaf6802fe1e76401bc5ab6e5fb85ed6c2d333e0c7"
    }

    fun getRequestHeaders(session: Session, extended: Boolean = true): Map<String, String> {
        return hashMapOf(
                "X-IG-App-Locale" to Locale.getDefault().toString(),
                "X-IG-Device-Locale" to Locale.getDefault().toString(),
                "X-Pigeon-Session-Id" to session.pigeonSessionId,
                "X-Pigeon-Rawclienttime" to "%.3f".format(System.currentTimeMillis() / 1000f),
                "X-IG-Connection-Speed" to "-1kbps",
                "X-IG-Bandwidth-Speed-KBPS" to "-1.000",
                "X-IG-Bandwidth-TotalBytes-B" to "0",
                "X-IG-Bandwidth-TotalTime-MS" to "0",
                "X-Bloks-Version-Id" to BLOKS_VERSION_ID,
                "X-MID" to (session.midToken.takeIf { extended } ?: ""),
                "X-IG-WWW-Claim" to (session.claimToken.takeIf { extended } ?: ""),
                "X-Bloks-Is-Layout-RTL" to "false",
                "X-IG-Device-ID" to session.uuid, // UUID4
                "X-IG-Android-ID" to session.androidId, // `android-${hash}`
                "X-IG-Connection-Type" to "WIFI",
                "X-IG-Capabilities" to "3brTvwE=",
                "X-IG-App-ID" to FACEBOOK_ANALYTICS_ID,
                "User-Agent" to buildUserAgent(session.deviceDPI, session.deviceResolution),
                "Accept-Language" to Locale.getDefault().toString().replace('_', '-'),
                "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
                "Accept-Encoding" to "gzip, deflate",
                "Host" to "i.instagram.com",
                "X-FB-HTTP-Engine" to "Liger",
                "Connection" to "close",
                "Cookie" to (CookieUtils.buildCookieHeader(session.cookieJar).takeIf { extended } ?: ""),
                "Authorization" to (session.authorizationToken.takeIf { extended } ?: "")
        ).filterNot { it.value.isBlank() }
    }


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
    private fun buildUserAgent(dpi: String,
                               resolution: String,
                               androidVersion: Int = android.os.Build.VERSION.SDK_INT,
                               androidRelease: String = android.os.Build.VERSION.RELEASE,
                               manufacturer: String = android.os.Build.MANUFACTURER,
                               brand: String = android.os.Build.BRAND.takeIf { !it.isNullOrBlank() }?.let { "/$it" } ?: "",
                               device: String = android.os.Build.DEVICE,
                               model: String = android.os.Build.MODEL,
                               hardware: String = android.os.Build.HARDWARE): String {

        return "Instagram $APP_VERSION Android ($androidVersion/$androidRelease; $dpi; $resolution; $manufacturer$brand; $model; $device; $hardware; ${Locale.getDefault()}; $VERSION_CODE)"
    }
}
