package io.karn.instagram.endpoints

import io.karn.instagram.Instagram
import io.karn.instagram.TestBase
import io.karn.instagram.core.SyntheticResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class SearchTest : TestBase() {

    @Test
    fun validateSearch() {
        val res = Instagram.getInstance().search.search("instagram")

        assertTrue(res is SyntheticResponse.ProfileSearch.Success)
    }
}
