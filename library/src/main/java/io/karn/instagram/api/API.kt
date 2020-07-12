package io.karn.instagram.api

import io.karn.instagram.common.CookieUtils
import io.karn.instagram.core.Session
import java.util.Locale

open class API {

    companion object {
        private val locale = Locale.getDefault().toString()
        private const val APP_VERSION = "148.0.0.33.121"
        private const val VERSION_CODE = "227298996"

        private const val FACEBOOK_ANALYTICS_ID = "567067343352427"

        private const val BLOKS_VERSION_ID = "5da07fc1b20eb4c7d1b2e6146ee5f197072cbbd193d2d1eb3bb4e825d3c39e28"
    }

    fun getRequestHeaders(session: Session): Map<String, String> {
        return hashMapOf(
                "X-IG-App-Locale" to locale,
                "X-IG-Device-Locale" to locale,
                "X-Pigeon-Session-Id" to session.pigeonSessionId,
                "X-Pigeon-Rawclienttime" to "%.3f".format(System.currentTimeMillis() / 1000f),
                "X-IG-Connection-Speed" to "-1kbps",
                "X-IG-Bandwidth-Speed-KBPS" to "-1.000",
                "X-IG-Bandwidth-TotalBytes-B" to "0",
                "X-IG-Bandwidth-TotalTime-MS" to "0",
                "X-Bloks-Version-Id" to BLOKS_VERSION_ID,
                "X-Bloks-Is-Layout-RTL" to "false",
                "X-IG-Device-ID" to session.uuid, // UUID4
                "X-IG-Android-ID" to session.androidId, // `android-${hash}`
                "X-IG-Connection-Type" to "WIFI",
                "X-IG-Capabilities" to "3brTvwE=",
                "X-IG-App-ID" to FACEBOOK_ANALYTICS_ID,
                "User-Agent" to buildUserAgent(session.deviceDPI, session.deviceResolution),
                "Accept-Language" to locale.replace('_', '-'),
                "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
                "Accept-Encoding" to "gzip, deflate",
                "Host" to "i.instagram.com",
                "X-FB-HTTP-Engine" to "Liger",
                "Connection" to "close",
                "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
                "Cookie" to CookieUtils.buildCookieHeader(session.cookieJar)
        ).also { headers ->
            session.cookieJar.getCookie("mid")?.value?.toString()?.also {
                headers["X-MID"] = it
            }
            session.cookieJar.getCookie("rur")?.value?.toString()?.also {
                headers["IG-U-RUR"] = it
            }
            session.cookieJar.getCookie("shbid")?.value?.toString()?.also {
                headers["IG-U-SHBID"] = it
            }
            session.cookieJar.getCookie("shbts")?.value?.toString()?.also {
                headers["IG-U-SHBTS"] = it
            }
            session.cookieJar.getCookie("ds_user_id")?.value?.toString()?.also {
                headers["IG-U-DS-USER-ID"] = it
            }
            session.claimToken.also {
                headers["X-IG-WWW-Claim"] = it
            }
            session.authorizationToken.takeUnless { it.isBlank() }?.also {
                headers["Authorization"] = it
            }
        }.filterNot { it.value.isBlank() }
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

        return "Instagram $APP_VERSION Android ($androidVersion/$androidRelease; $dpi; $resolution; $manufacturer$brand; $model; $device; $hardware; $locale; $VERSION_CODE)"
    }
}
