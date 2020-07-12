package io.karn.instagram.endpoints

import io.karn.instagram.Instagram
import io.karn.instagram.api.MediaAPI
import io.karn.instagram.common.wrapAPIException
import io.karn.instagram.core.SyntheticResponse
import io.karn.instagram.exceptions.InstagramAPIException
import org.json.JSONArray

class Media internal constructor(private val instagram: Instagram) {
    /**
     *
     */
    fun getMediaFromShortKey(shortKey: String): SyntheticResponse.MediaInfo {
        val (res, error) = wrapAPIException { MediaAPI.getInfoFromShortKey(instagram.session, shortKey) }

        res ?: return SyntheticResponse.MediaInfo.Failure(error!!)

        return when (res.statusCode) {
            200 -> {
                val media = res.jsonObject.optJSONObject("graphql")?.optJSONObject("shortcode_media")

                media ?: return SyntheticResponse.MediaInfo.Failure(InstagramAPIException(404, "Unable to fetch valid response for media"))

                SyntheticResponse.MediaInfo.Success(media)
            }
            else-> SyntheticResponse.MediaInfo.Failure(InstagramAPIException(res.statusCode, res.text))
        }
    }

    /**
     * Create a SyntheticResponse from the response of a media likes API request.
     *
     * @param mediaKey    The Media Key associated with the post.
     * @return  A {@link SyntheticResponse.MediaLikess} object.
     */
    fun getLikes(mediaKey: String): SyntheticResponse.MediaLikes {
        val (res, error) = wrapAPIException { MediaAPI.getLikes(instagram.session, mediaKey) }

        res ?: return SyntheticResponse.MediaLikes.Failure(error!!)

        // Handle error messages.
        return when (res.statusCode) {
            200 -> SyntheticResponse.MediaLikes.Success(res.jsonObject.optJSONArray("users") ?: JSONArray())
            else -> SyntheticResponse.MediaLikes.Failure(InstagramAPIException(res.statusCode, res.text))
        }
    }

    /**
     * Create a SyntheticResponse from the response of a media comments API request.
     *
     * @param mediaKey    The Media Key associated with the post.
     * @return  A {@link SyntheticResponse.MediaLikess} object.
     */
    fun getComments(mediaKey: String): SyntheticResponse.MediaComments {
        val (res, error) = wrapAPIException { MediaAPI.getComments(instagram.session, mediaKey) }

        res ?: return SyntheticResponse.MediaComments.Failure(error!!)

        // Handle error messages.
        return when (res.statusCode) {
            200 -> SyntheticResponse.MediaComments.Success(res.jsonObject.optJSONArray("comments") ?: JSONArray())
            else -> SyntheticResponse.MediaComments.Failure(InstagramAPIException(res.statusCode, res.text))
        }
    }
}
