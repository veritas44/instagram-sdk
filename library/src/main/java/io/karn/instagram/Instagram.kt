package io.karn.instagram

import android.content.Context
import io.karn.instagram.common.CookieUtils
import io.karn.instagram.core.Configuration
import io.karn.instagram.core.Session
import io.karn.instagram.endpoints.*
import khttp.KHttpConfig
import khttp.structures.cookie.CookieJar
import org.json.JSONObject

typealias HttpResponse = khttp.responses.Response

/**
 * The 'Instagram' class is the primary entry point for SDK related functions. Be sure to execute the
 * {@link #init(Configuration)} function to initialize the library with default or custom configuration.
 *
 * Note the the SDK itself is synchronous and allows the developer the flexibility to implement their
 * preferred async pattern.
 */
class Instagram private constructor(internal val configuration: Configuration) {

    companion object {
        internal lateinit var instance: Instagram

        /**
         * Initialize the Instagram SDK with the provided configuration. This function must be executed before other
         * parts of the library are interacted with.
         */
        fun init(context: Context, configure: (Configuration.() -> Unit) = {}) {
            // Initialize the Configuration.

            val displayMetrics = context.resources.displayMetrics
            val config = Configuration(
                    deviceDPI = "${displayMetrics.densityDpi}dpi",
                    deviceResolution = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
            )

            // Apply any changes.
            config.configure()

            // Build instance.
            instance = Instagram(config)
        }

        fun getInstance(): Instagram {
            return instance
        }
    }

    var session: Session = Session()
        internal set(value) {
//            if (field == value) {
//                return
//            }

            field = value
            configuration.sessionUpdateListener?.invoke(session)
        }

    val authentication: Authentication = Authentication(this)
    val account: Account = Account(this)
    val search: Search = Search(this)
    val stories: Stories = Stories(this)
    val media: Media = Media(this)
    val directMessages: DirectMessages = DirectMessages(this)

    /**
     * Constructs the session to be used with all requests. The instance ID should be a unique
     * key since it is used to deterministically generate the various uuids.
     *
     * @param instanceId A unique key that is used to persist the session.
     * @param data serialized data containing session metadata.
     */
    fun initializeSession(instanceId: String, data: String) {
        val jsonData = JSONObject(data.ifBlank { "{}" })

        session = session.copy(instanceId = instanceId,
                primaryKey = jsonData.optString("primaryKey", session.primaryKey),
                csrfToken = jsonData.optString("csrfToken", session.csrfToken),
                midToken = jsonData.optString("midToken", session.midToken),
                claimToken = jsonData.optString("claimToken", session.claimToken),
                authorizationToken = jsonData.optString("authorizationToken", session.authorizationToken),
                cookieJar = CookieUtils.deserializeFromJson(jsonData.optJSONArray("cookies"), session.cookieJar)
        )
    }

    init {
        KHttpConfig.attachInterceptor { response ->
            if (response.headers.any { it.value.contains("application/json") }) {

                // Update the session with new values
                session = session.copy(
                        // Update values if needed or default to existing values
                        csrfToken = response.cookies.getCookie("csrftoken")?.value?.toString() ?: session.csrfToken,
                        midToken = response.headers["ig-set-x-mid"]?.takeUnless { it.isBlank() } ?: session.midToken,
                        claimToken = response.headers["x-ig-set-www-claim"]?.takeUnless { it.isBlank() } ?: session.claimToken,
                        authorizationToken = response.headers["ig-set-authorization"]?.takeUnless { it.isBlank() || it == "Bearer IGT:2:" } ?: session.authorizationToken,
                        publicKeyId = response.headers["ig-set-password-encryption-key-id"]?.takeUnless { it.isBlank() }?.toIntOrNull() ?: session.publicKeyId,
                        publicKey = response.headers["ig-set-password-encryption-pub-key"]?.takeUnless { it.isBlank() } ?: session.publicKey,
                        cookieJar = CookieJar(session.cookieJar + response.cookies))
            }

            // Write response to logger
            configuration.requestLogger?.invoke(response)
        }
    }
}
