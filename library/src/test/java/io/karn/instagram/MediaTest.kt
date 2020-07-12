package io.karn.instagram

import io.karn.instagram.core.SyntheticResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class MediaTest : TestBase() {

    private val mediaKey = "1846492913375143728"

    @Test
    fun validateMediaFromShortKey() {
        val res = Instagram.getInstance().media.getMediaFromShortKey("B8b2CVngQNQ")

        assert(res is SyntheticResponse.MediaInfo.Success)
    }


    @Test
    fun validateMediaLikes() {
        val res = Instagram.getInstance().media.getLikes(mediaKey)

        println(res)

        assertTrue(res is SyntheticResponse.MediaLikes.Success)
        assertTrue(res.likes.length() > 0)
    }

    @Test
    fun validateMediaComments() {
        val res = Instagram.getInstance().media.getComments(mediaKey)

        System.out.println(res)

        assertTrue(res is SyntheticResponse.MediaComments.Success)
        assertTrue(res.comments.length() > 0)
    }
}
