package io.karn.instagram.api

import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get

internal object SearchAPI : API() {

    fun search(session: Session, query: String) =
            get(url = Endpoints.SEARCH,
                    params = mapOf(
                            "rank_token" to session.rankToken,
                            "is_typeahead" to "false",
                            "query" to query
                    ),
                    headers = getRequestHeaders(session),
                    cookies = session.cookieJar)
}
