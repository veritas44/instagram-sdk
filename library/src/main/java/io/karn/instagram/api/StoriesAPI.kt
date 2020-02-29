package io.karn.instagram.api

import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get

internal object StoriesAPI : API() {

    fun getStories(session: Session, primaryKey: String) =
            get(url = String.format(Endpoints.STORIES, primaryKey),
                    headers = getRequestHeaders(session),
                    cookies = session.cookieJar)
}
