package io.karn.instagram.api

import io.karn.instagram.core.Crypto
import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get

internal object SearchAPI {

    fun search(query: String, session: Session) =
            get(url = Endpoints.SEARCH,
                    params = mapOf(
                            "rank_token" to "${session.primaryKey}_${session.uuid}",
                            "is_typeahead" to "false",
                            "query" to query
                    ),
                    headers = Crypto.HEADERS,
                    cookies = session.cookieJar)
}
