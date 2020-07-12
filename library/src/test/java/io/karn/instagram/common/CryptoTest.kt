package io.karn.instagram.common

import io.karn.instagram.api.MediaAPI
import io.karn.instagram.core.Crypto
import io.karn.instagram.core.Session
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class CryptoTest {

    @Test
    fun generateSignedBodyTest() {
        val data = JSONObject()
                .put("_csrftoken", "token")
                .put("guid", "uuid")
                .put("device_id", "android-id")
                .put("challenge_context", """{"step_name": "select_verify_method", "nonce_code": "code", "user_id": 0000000000, "is_stateless": false}""")

        assertEquals("signed_body=SIGNATURE.%7B%22device_id%22%3A%22android-id%22%2C%22guid%22%3A%22uuid%22%2C%22_csrftoken%22%3A%22token%22%2C%22challenge_context%22%3A%22%7B%5C%22step_name%5C%22%3A+%5C%22select_verify_method%5C%22%2C+%5C%22nonce_code%5C%22%3A+%5C%22code%5C%22%2C+%5C%22user_id%5C%22%3A+0000000000%2C+%5C%22is_stateless%5C%22%3A+false%7D%22%7D", Crypto.generateSignedBody(data.toString()))
    }

    @Test
    fun getInfo() {
        println(MediaAPI.getInfoFromShortKey(Session(), "B8b2CVngQNQ").text)

        // assert(res is SyntheticResponse.MediaInfo.Success)
    }
}
