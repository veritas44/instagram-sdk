package io.karn.instagram.endpoints

import io.karn.instagram.Instagram
import io.karn.instagram.api.AuthenticationAPI
import io.karn.instagram.api.AuthenticationBootstrapAPI
import io.karn.instagram.common.Errors
import io.karn.instagram.common.wrapAPIException
import io.karn.instagram.core.Crypto
import io.karn.instagram.core.SyntheticResponse
import io.karn.instagram.exceptions.InstagramAPIException
import khttp.responses.Response
import org.json.JSONObject

/**
 * Authentication
 *
 * Instagram authentication uses a cookie based authentication mechanism in addition to the CSRF token. Below are the
 * Steps as well as the corresponding SDK functions which perform said step.
 *
 * 1. Admit one - Fetching the original token.
 *   The first step in the authentication process is to retrieve a token with can be used to authenticate. This token is
 *   then used, along with the account credentials to sign the user in.
 *
 *   We will defer to the next step for the SDK function which handles these together.
 *
 * 2. Authentication
 *   The next step is to authenticate the user. The corresponding SDK function is [Authentication.authenticate].
 *
 *   The resulting SyntheticResponse maps the following states:
 *   - Success -> The account has been authenticated, the result contains the serialized Cookies as well as the UUID used.
 *                  The Cookies and UUID are used to restore the session after a cold start via [Session.buildSession].
 *   - TwoFactorAuth -> The account has been authenticated but requires a two-factor code to authorize the login. A code
 *                      will be sent to the primary two-factor method (phone number/email) associated with the account.
 *                      The authentication can be completed via the [Authentication.twoFactor] function.
 *   - ChallengeRequired -> The account has been flagged by the server for a suspicious login, a verification flow needs
 *                          to be followed. The path for the challenge is provided and must be queried to
 *   - InvalidCredentials ->
 *   - ApiFailure ->
 */
class Authentication internal constructor(private val instagram: Instagram) {

    companion object {
        private const val EXPERIMENTS = "ig_growth_android_profile_pic_prefill_with_fb_pic_2,ig_android_email_fuzzy_matching_universe,ig_android_recovery_one_tap_holdout_universe,ig_android_gmail_oauth_in_reg,ig_android_reg_modularization_universe,ig_android_sim_info_upload,ig_android_device_verification_fb_signup,ig_android_reg_nux_headers_cleanup_universe,ig_android_direct_main_tab_universe_v2,ig_android_sign_in_help_only_one_account_family_universe,ig_android_account_linking_upsell_universe,ig_android_enable_keyboardlistener_redesign,ig_android_suma_landing_page,ig_android_notification_unpack_universe,ig_android_access_flow_prefill,ig_android_shortcuts_2019,ig_android_ask_for_permissions_on_reg,ig_android_device_based_country_verification,ig_account_identity_logged_out_signals_global_holdout_universe,ig_video_debug_overlay,ig_android_caption_typeahead_fix_on_o_universe,ig_android_retry_create_account_universe,ig_android_video_ffmpegutil_pts_fix,ig_android_quickcapture_keep_screen_on,ig_android_smartlock_hints_universe,ig_android_login_identifier_fuzzy_match,ig_android_passwordless_account_password_creation_universe,ig_android_black_out_toggle_universe,ig_android_security_intent_switchoff,ig_android_mobile_http_flow_device_universe,ig_android_get_cookie_with_concurrent_session_universe,ig_android_multi_tap_login_new,ig_android_nux_add_email_device,ig_android_device_info_foreground_reporting,ig_android_fb_account_linking_sampling_freq_universe,ig_android_vc_interop_use_test_igid_universe,ig_android_device_verification_separate_endpoint,ig_assisted_login_universe,ig_android_video_render_codec_low_memory_gc,ig_android_device_detection_info_upload,ig_android_direct_add_direct_to_android_native_photo_share_sheet,ig_android_sms_retriever_backtest_universe"

        const val AUTH_METHOD_EMAIL = "email"
        const val AUTH_METHOD_PHONE = "phone"
    }

    fun bootstrap(): SyntheticResponse.Bootstrap {

        // Token
        val (tokenRes, tokenErr) = wrapAPIException { AuthenticationBootstrapAPI.getToken(instagram.session) }
        tokenRes ?: return SyntheticResponse.Bootstrap.Failure(tokenErr!!)

        // QE sync
        var data = JSONObject()
                .put("id", instagram.session.uuid)
                .put("server_config_retrieval", "1")

        val (syncRes, syncErr) = wrapAPIException { AuthenticationBootstrapAPI.sync(instagram.session, data) }
        syncRes ?: return SyntheticResponse.Bootstrap.Failure(syncErr!!)

        // Store the first set of cookies here
        instagram.session.publicKeyId = syncRes.headers.getOrElse("ig-set-password-encryption-key-id") { "209" }.toInt()
        instagram.session.publicKey = syncRes.headers.getOrElse("ig-set-password-encryption-pub-key") { "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUFvSkw5RGQzdWliYmRlOWJVYXlDOQpIMXVJb0RsL3BxeEd3Yjd3dGx6cjRSODhwbGI0SUs1aEdUQ2VTN0xUTXBUNk5oWVFGT2VhajhtcitjVlp1Y1FuCmxQUVNiZTJpM3lIbU9DV2h6L0s0WStzRU1lYmJvZUpuZHpPODFPVVhkUjNZWVN3STJTSFdYTTB0VnhRQjlmZjYKZW0xU3QrSkF6MnhhMDBBMTFod1BraUpIOTdGbU54eWlqL2wrcEdEbXJCQUVLbFNMUzQvdGhGNUNmMEpIVFFwbwpDUkU3VjJDaEtTRlQzNVIvY01TdHR2ekdoQ2dtY1Z5M092aTR5d0VCSkpoTGVrQmV1cG5OWTUvL08rOUxobEhwCmVIcVN1cG9MazZSbDhtTGJkK3ptWTRoWVRzeExDRnpQcDJNSGI1NXZ5eWMxRTdJK1RjcVNXMjFQemlyNWFQcWwKYlFJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==" }

        // Prefill
        val (prefillRes, prefillErr) = wrapAPIException { AuthenticationBootstrapAPI.prefillCandidates(instagram.session) }
        prefillRes ?: return SyntheticResponse.Bootstrap.Failure(prefillErr!!)

        // QE sync with experiments
        data = JSONObject()
                .put("id", instagram.session.uuid)
                .put("server_config_retrieval", "1")
                .put("experiments", EXPERIMENTS)

        val (syncExperimentsRes, syncExperimentsErr) = wrapAPIException { AuthenticationBootstrapAPI.sync(instagram.session, data) }
        syncExperimentsRes ?: return SyntheticResponse.Bootstrap.Failure(syncExperimentsErr!!)

        // Contact prefill
        val (contactPrefillRes, contactPrefillErr) = wrapAPIException { AuthenticationBootstrapAPI.contactPointPrefill(instagram.session) }
        contactPrefillRes ?: return SyntheticResponse.Bootstrap.Failure(contactPrefillErr!!)

        // QE sync with new metadata
        data = JSONObject()
                .put("id", instagram.session.uuid)
                .put("_csrftoken", instagram.session.csrfToken)
                .put("server_config_retrieval", "1")

        val (syncWithHeadersRes, syncWithHeadersErr) = wrapAPIException { AuthenticationBootstrapAPI.sync(instagram.session, data) }
        syncWithHeadersRes ?: return SyntheticResponse.Bootstrap.Failure(syncWithHeadersErr!!)

        // QE sync with new metadata and experiments
        data = JSONObject()
                .put("id", instagram.session.uuid)
                .put("_csrftoken", instagram.session.csrfToken)
                .put("server_config_retrieval", "1")
                .put("experiments", EXPERIMENTS)

        val (syncExperimentsWithHeadersRes, syncExperimentsWithHeadersErr) = wrapAPIException { AuthenticationBootstrapAPI.sync(instagram.session, data) }
        syncExperimentsWithHeadersRes ?: return SyntheticResponse.Bootstrap.Failure(syncExperimentsWithHeadersErr!!)

        // Prep for login

        if (instagram.session.csrfToken.isBlank()) {
            return SyntheticResponse.Bootstrap.Failure(InstagramAPIException(412, "Unable to fetch token for user"))
        }

        return SyntheticResponse.Bootstrap.Success(JSONObject().put("token", instagram.session.csrfToken))
    }


    private fun postLogin(): SyntheticResponse.Bootstrap {

        val userId = instagram.session.cookieJar.getCookie("ds_user_id")?.value?.toString()
        if (userId.isNullOrBlank()) {
            println("User ID is blank")
        }

        // QE sync with new metadata
        var data = JSONObject()
                .put("id", userId)
                .put("_uid", userId)
                .put("_uuid", instagram.session.uuid)
                .put("_csrftoken", instagram.session.csrfToken)
                .put("server_config_retrieval", "1")

        println("Sync post login: $data")

        val (syncWithHeadersRes, syncWithHeadersErr) = wrapAPIException { AuthenticationBootstrapAPI.sync(instagram.session, data) }
        syncWithHeadersRes ?: return SyntheticResponse.Bootstrap.Failure(syncWithHeadersErr!!)

        // QE sync with new metadata and experiments
        data = JSONObject()
                .put("id", userId)
                .put("_uid", userId)
                .put("_uuid", instagram.session.uuid)
                .put("_csrftoken", instagram.session.csrfToken)
                .put("server_config_retrieval", "1")
                .put("experiments", EXPERIMENTS)

        val (syncExperimentsWithHeadersRes, syncExperimentsWithHeadersErr) = wrapAPIException { AuthenticationBootstrapAPI.sync(instagram.session, data) }
        syncExperimentsWithHeadersRes ?: return SyntheticResponse.Bootstrap.Failure(syncExperimentsWithHeadersErr!!)

        // Token refresh once more, cookies are saved here
        val (tokenRes, tokenErr) = wrapAPIException { AuthenticationBootstrapAPI.getToken(instagram.session) }
        tokenRes ?: return SyntheticResponse.Bootstrap.Failure(tokenErr!!)

        return SyntheticResponse.Bootstrap.Success(JSONObject())
    }


    /**
     * Creates a SyntheticResponse from the response of a authentication API request.
     *
     * @param username  The username of the account that is being authenticated. Usernames must follow the platform
     *                  requirements; usernames with single spaces in place of an userscore are known to work but should
     *                  not be used.
     * @param password  The corresponding password.
     * @param token     An optional auth token to skip the CSRF token phase.
     * @return  A [SyntheticResponse.Auth] object.
     */
    fun authenticate(username: String, password: String, token: String = instagram.session.csrfToken): SyntheticResponse.Auth {
        if (!token.isBlank()) {
            // Go straight to login.
            return processLogin(username, password, token)
        }

        return when (val res = bootstrap()) {
            is SyntheticResponse.Bootstrap.Success -> authenticate(username, password, res.data.optString("token"))
            is SyntheticResponse.Bootstrap.Failure -> SyntheticResponse.Auth.TokenFailure(res.exception.statusCode, res.exception.statusMessage)
        }
    }

    /**
     * Creates a SyntheticResponse from the response of a two factor login API request.
     *
     * @param code          The two factor code that was sent to the user's phone/email.
     * @param identifier    The two factor login identifier token.
     * @param username      The username of the account that is being authenticated. See: [authenticate]
     * @param password      The password of the account that is being authenticated. See: [authenticate]
     * @return A [SyntheticResponse.TwoFactorResult] object.
     */
    fun twoFactorLogin(code: String, identifier: String, token: String, username: String, password: String): SyntheticResponse.TwoFactorResult {
        val data = Crypto.generateTwoFactorPayload(instagram.session, code.replace("\\s".toRegex(), ""), identifier, token, username, password)

        val (res, error) = wrapAPIException { AuthenticationAPI.twoFactor(instagram.session, data) }

        res ?: return SyntheticResponse.TwoFactorResult.Failure(error!!)

        return when (res.statusCode) {
            200 -> SyntheticResponse.TwoFactorResult.Success(buildSuccess(res))
            else -> SyntheticResponse.TwoFactorResult.Failure(InstagramAPIException(res.statusCode, res.jsonObject.optString("message", Errors.ERROR_UNKNOWN)))
        }
    }

    /**
     * Creates a SyntheticResponse from the response of an auth preperation API request.
     *
     * @param path      The path of the auth challenge API as returned from the corresponding sentry.
     * @param nonce     The nonce of the auth challenge API as returned from the corresponding sentry.
     * @param userId    The userId in the auth challenge API as returned from the corresponding sentry.
     */
    fun prepareAuthChallenge(path: String, nonce: String, userId: String): SyntheticResponse.ChallengeResult {
        val (res, error) = wrapAPIException { AuthenticationAPI.prepareAuthChallenge(instagram.session, path, nonce, userId) }

        res ?: return SyntheticResponse.ChallengeResult.Failure(error!!)

        return when (res.statusCode) {
            200 -> {
                when (res.jsonObject.optString("step_name")) {
                    "select_verify_method" -> SyntheticResponse.ChallengeResult.Success(res.jsonObject)
                    "delta_login_review" -> SyntheticResponse.ChallengeResult.Success(res.jsonObject)
                    else -> SyntheticResponse.ChallengeResult.Failure(InstagramAPIException(res.statusCode, res.jsonObject.toString()))
                }
            }
            else -> SyntheticResponse.ChallengeResult.Failure(InstagramAPIException(res.statusCode, res.jsonObject.optString("message", Errors.ERROR_UNKNOWN)))
        }
    }

    fun selectAuthChallengeMethod(path: String, method: String, nonce: String, userId: String): SyntheticResponse.AuthMethodSelectionResult {
        val data = Crypto.generateAuthenticatedChallengeParams(instagram.session) {
            it.put("choice", if (AUTH_METHOD_PHONE == method) 0 else 1)
            it.put("challenge_context", """{"step_name": "select_verify_method", "nonce_code": "$nonce", "user_id": $userId, "is_stateless": false}""")
        }

        val (res, error) = wrapAPIException { AuthenticationAPI.selectAuthChallengeMethod(instagram.session, path, data) }

        res ?: return SyntheticResponse.AuthMethodSelectionResult.Failure(error!!)

        return when (res.statusCode) {
            200 -> {
                when (res.jsonObject.optString("step_name")) {
                    "verify_code" -> SyntheticResponse.AuthMethodSelectionResult.PhoneSelectionSuccess(res.jsonObject.optJSONObject("step_data")
                            ?: JSONObject())
                    "verify_email" -> SyntheticResponse.AuthMethodSelectionResult.EmailSelectionSuccess(res.jsonObject.optJSONObject("step_data")
                            ?: JSONObject())
                    else -> SyntheticResponse.AuthMethodSelectionResult.Failure(InstagramAPIException(res.statusCode, res.jsonObject.toString()))
                }
            }
            else -> SyntheticResponse.AuthMethodSelectionResult.Failure(InstagramAPIException(res.statusCode, res.jsonObject.optString("message", Errors.ERROR_UNKNOWN)))
        }
    }

    fun submitChallengeCode(path: String, code: String, nonce: String, userId: String): SyntheticResponse.ChallengeCodeSubmitResult {
        val data = Crypto.generateAuthenticatedChallengeParams(instagram.session) {
            it.put("security_code", code)
            it.put("challenge_context", """{"step_name": "select_verify_method", "nonce_code": "$nonce", "user_id": $userId, "is_stateless": false}""")
        }

        val (res, error) = wrapAPIException { AuthenticationAPI.submitAuthChallenge(instagram.session, path, data) }

        res ?: return SyntheticResponse.ChallengeCodeSubmitResult.Failure(error!!)

        return when (res.statusCode) {
            200 -> {
                if (res.jsonObject.optString("logged_in_user").isNullOrBlank()) {
                    SyntheticResponse.ChallengeCodeSubmitResult.Failure(InstagramAPIException(412, res.jsonObject.toString()))
                } else {
                    SyntheticResponse.ChallengeCodeSubmitResult.Success(buildSuccess(res))
                }
            }
            else -> SyntheticResponse.ChallengeCodeSubmitResult.Failure(InstagramAPIException(res.statusCode, res.jsonObject.optString("message", Errors.ERROR_UNKNOWN)))
        }
    }

    fun logoutUser(): SyntheticResponse.Logout {
        val (res, error) = wrapAPIException { AuthenticationAPI.logout(instagram.session) }

        res ?: return SyntheticResponse.Logout.Failure(error!!)

        return when (res.statusCode) {
            200 -> SyntheticResponse.Logout.Success(res.statusCode)
            else -> SyntheticResponse.Logout.Failure(InstagramAPIException(res.statusCode, res.jsonObject.optString("message", Errors.ERROR_UNKNOWN)))
        }
    }

    private fun processLogin(username: String, password: String, token: String): SyntheticResponse.Auth {
        // Generate the login payload.
        val data = Crypto.generateLoginPayload(instagram.session, username, password, 1)

        val (res, error) = wrapAPIException { AuthenticationAPI.login(instagram.session, data) }

        res ?: return SyntheticResponse.Auth.Failure(error!!)

        return when (res.statusCode) {
            200 -> SyntheticResponse.Auth.Success(buildSuccess(res))
            400 -> {
                when {
                    res.jsonObject.optBoolean("two_factor_required") -> {
                        // User requires two factor.
                        SyntheticResponse.Auth.TwoFactorRequired(res.jsonObject
                                .put("token", token)
                                .put("device_id", instagram.session.androidId))
                    }
                    res.jsonObject.has("challenge") -> {
                        // User needs to pass challenge
                        SyntheticResponse.Auth.ChallengeRequired(res.jsonObject.getJSONObject("challenge"))
                    }
                    res.jsonObject.optBoolean("invalid_credentials") -> {
                        SyntheticResponse.Auth.InvalidCredentials(Errors.ERROR_INVALID_CREDENTIALS)
                    }
                    else -> SyntheticResponse.Auth.Failure(InstagramAPIException(res.statusCode, res.text))
                }
            }
            else -> SyntheticResponse.Auth.Failure(InstagramAPIException(res.statusCode, res.text))
        }
    }

    private fun buildSuccess(res: Response): JSONObject {
        // Store the credentials
        instagram.session.claimToken = res.headers.getOrElse("x-ig-set-www-claim") { "" }
        instagram.session.authorizationToken = res.headers.getOrElse("ig-set-authorization") { "" }

        val post = postLogin()

        println(post)

        return res.jsonObject.put("sdk_data", instagram.session.serialize())
    }
}
