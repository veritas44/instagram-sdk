package io.karn.instagram

import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class InitializationTest {

    @Test
    fun startUp_validateInitialization() {
        // Initialize the library
        Instagram.init(ApplicationProvider.getApplicationContext()) {
            requestLogger = { response -> }
            sessionUpdateListener = { session -> }
        }

        assertNotNull(Instagram.getInstance().session)
        assertNotNull(Instagram.getInstance().authentication)
        assertNotNull(Instagram.getInstance().account)
        assertNotNull(Instagram.getInstance().search)
        assertNotNull(Instagram.getInstance().stories)
    }
}
