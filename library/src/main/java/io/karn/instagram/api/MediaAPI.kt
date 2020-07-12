package io.karn.instagram.api

import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get

internal object MediaAPI : API() {
    fun getInfoFromShortKey(session: Session, shortKey: String) =
            get(url = String.format(Endpoints.MEDIA_INFO, shortKey),
                    params = mapOf(
                            "__a" to "1"
                    ),
                    headers = getRequestHeaders(session))

    fun getLikes(session: Session, mediaKey: String) =
            get(url = String.format(Endpoints.MEDIA_LIKES, mediaKey),
                    params = mapOf(
                            "rank_token" to session.rankToken,
                            "max_id" to ""
                    ),
                    headers = getRequestHeaders(session))

    fun getComments(session: Session, mediaKey: String) =
            get(url = String.format(Endpoints.MEDIA_COMMENTS, mediaKey),
                    params = mapOf(
                            "rank_token" to session.rankToken,
                            "max_id" to ""
                    ),
                    headers = getRequestHeaders(session))
}
