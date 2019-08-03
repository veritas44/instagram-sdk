package io.karn.instagram.api

import io.karn.instagram.core.Crypto
import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get

internal object MediaAPI {
    fun getLikes(mediaKey: String, session: Session) =
            get(url = String.format(Endpoints.MEDIA_LIKES, mediaKey),
                    params = mapOf(
                            "rank_token" to "${session.primaryKey}_${session.uuid}",
                            "max_id" to ""
                    ),
                    headers = Crypto.HEADERS,
                    cookies = session.cookieJar)

    fun getComments(mediaKey: String, session: Session) =
            get(url = String.format(Endpoints.MEDIA_COMMENTS, mediaKey),
                    params = mapOf(
                            "rank_token" to "${session.primaryKey}_${session.uuid}",
                            "max_id" to ""
                    ),
                    headers = Crypto.HEADERS,
                    cookies = session.cookieJar)
}
