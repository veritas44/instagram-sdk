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
        const val AUTH_METHOD_EMAIL = "email"
        const val AUTH_METHOD_PHONE = "phone"
    }

    fun bootstrap(): SyntheticResponse.Bootstrap {

        val headerData = JSONObject()
                .put("mobile_subno_usage", "ig_select_app")
                .put("device_id", instagram.session.uuid)

        val (headersRes, headersErr) = wrapAPIException { AuthenticationBootstrapAPI.getHeaders(instagram.session, headerData) }

        headersRes ?: return SyntheticResponse.Bootstrap.Failure(headersErr!!)

        println("csrf: ${instagram.session.csrfToken}")
        println("headers: ${headersRes.headers}")

        val syncData = JSONObject()
                .put("csrftoken", instagram.session.csrfToken)
                .put("id", instagram.session.uuid)
                .put("server_config_retrieval", "1")

        val (syncRes, syncErr) = wrapAPIException { AuthenticationBootstrapAPI.getSync(instagram.session, syncData) }

        syncRes ?: return SyntheticResponse.Bootstrap.Failure(syncErr!!)

        println("headers: ${syncRes.headers}")

        if (instagram.session.csrfToken.isBlank()) {
            return SyntheticResponse.Bootstrap.Failure(InstagramAPIException(412, "Unable to fetch token for use"))
        }

        return SyntheticResponse.Bootstrap.Success(JSONObject().put("token", instagram.session.csrfToken))
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
     * @param path  The path of the auth challenge API as returned from the corresponding sentry.
     */
    fun prepareAuthChallenge(path: String): SyntheticResponse.ChallengeResult {
        val (res, error) = wrapAPIException { AuthenticationAPI.prepareAuthChallenge(instagram.session, path) }

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

    fun selectAuthChallengeMethod(path: String, method: String): SyntheticResponse.AuthMethodSelectionResult {
        val data = Crypto.generateAuthenticatedChallengeParams(instagram.session) {
            it.put("choice", if (AUTH_METHOD_PHONE == method) 0 else 1)
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

    fun submitChallengeCode(path: String, code: String): SyntheticResponse.ChallengeCodeSubmitResult {
        val data = Crypto.generateAuthenticatedChallengeParams(instagram.session) {
            it.put("security_code", code)
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
        val data = Crypto.generateLoginPayload(instagram.session, token, username, password, 1)

        val (res, error) = wrapAPIException { AuthenticationAPI.login(instagram.session, data) }

        res ?: return SyntheticResponse.Auth.Failure(error!!)

        return when (res.statusCode) {
            200 -> SyntheticResponse.Auth.Success(buildSuccess(res))
            400 -> {
                // println(res.jsonObject.toString(4))

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
        instagram.session = instagram.session.copy(primaryKey = res.jsonObject.optJSONObject("logged_in_user")?.optString("pk") ?: "")

        return res.jsonObject.put("sdk_data", instagram.session.serialize())
    }
}
