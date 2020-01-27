package io.karn.instagram

import androidx.test.core.app.ApplicationProvider
import io.karn.instagram.core.SyntheticResponse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

open class TestBase {
    companion object {
        var intialized = false
    }

    init {
        if (!intialized) {
            intialized = true

            System.out.println("Initializing")

            // Initialize the library
            Instagram.init(ApplicationProvider.getApplicationContext()) {
                requestLogger = { response -> }
            }

            val username = System.getenv("DEFAULT_USERNAME")
                    ?: throw IllegalStateException("No username specified.")
            val password = System.getenv("DEFAULT_PASSWORD")
                    ?: throw IllegalStateException("No password specified.")

            // Authenticate the user.
            val res = Instagram.getInstance().authentication.authenticate(username, password)

            System.out.println(res)

            assertTrue(res is SyntheticResponse.Auth.Success)

            assertNotNull(res.data.optJSONObject("logged_in_user"))
            assertNotNull(res.data.optJSONArray("cookie"))
            assertNotEquals("", res.data.optString("uuid"))

            assertNotNull(res)

            System.out.println("Done initializing.")

            assertNotEquals("0", Instagram.session.wwwClaim)

            System.out.println("Done initializing ${Instagram.session.wwwClaim}.")
        }
    }
}
