package io.karn.instagram

import androidx.test.core.app.ApplicationProvider
import io.karn.instagram.core.SyntheticResponse
import kotlin.random.Random
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

open class TestBase {
    companion object {
        var intialized = false
    }

    init {
        if (!intialized) {
            intialized = true

            println("Initializing")

            // Initialize the library
            Instagram.init(ApplicationProvider.getApplicationContext()) {
                requestLogger = { response -> }
                sessionUpdateListener = { session -> }
            }
            Instagram.getInstance().session = Instagram.getInstance().session.copy(instanceId = Random(System.currentTimeMillis()).toString())

            val username = System.getenv("DEFAULT_USERNAME") ?: throw IllegalStateException("No username specified.")
            val password = System.getenv("DEFAULT_PASSWORD") ?: throw IllegalStateException("No password specified.")

            // Authenticate the user.
            val res = Instagram.getInstance().authentication.authenticate(username, password)

            assertNotNull(res)

            when (res) {
                is SyntheticResponse.Auth.Success -> {
                    assertNotNull(res.data.optJSONObject("logged_in_user"))
                    assertNotEquals("", res.data.optString("sdk_data"))
                }
                else -> throw IllegalStateException("Failed to login")
            }

            println("Done initializing.")
        }
    }
}
