package io.karn.instagram

import io.karn.instagram.core.SyntheticResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class LogoutTest : TestBase() {

    @Test
    fun validateLogout() {
        val res = Instagram.getInstance().authentication.logoutUser()

        assertTrue(res is SyntheticResponse.Logout.Success)
    }
}
