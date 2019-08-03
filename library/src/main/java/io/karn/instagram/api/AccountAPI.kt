package io.karn.instagram.api

import io.karn.instagram.core.Crypto
import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get
import khttp.post

internal object AccountAPI {

    fun accountInfo(primaryKey: String, session: Session) =
            get(url = String.format(Endpoints.ACCOUNT_INFO, primaryKey),
                    headers = Crypto.HEADERS,
                    cookies = session.cookieJar)

    fun feed(feedUserKey: String, maxId: String, minTimestamp: String, session: Session) =
            get(url = String.format(Endpoints.ACCOUNT_FEED, feedUserKey),
                    params = mapOf(
                            "max_id" to maxId,
                            "min_timestamp" to minTimestamp,
                            "rank_token" to "${session.primaryKey}_${session.uuid}",
                            "ranked_content" to "true"
                    ),
                    headers = Crypto.HEADERS,
                    cookies = session.cookieJar)

    fun relationships(endpoint: String, userKey: String, maxId: String, session: Session) =
            get(url = String.format(endpoint, userKey),
                    params = mapOf(
                            "rank_token" to "${session.primaryKey}_${session.uuid}",
                            "max_id" to maxId
                    ),
                    headers = Crypto.HEADERS,
                    cookies = session.cookieJar)

    fun updateRelationship(endpoint: String, userKey: String, session: Session) =
            post(url = String.format(endpoint, userKey),
                    headers = Crypto.HEADERS,
                    data = Crypto.generateAuthenticatedParams(session) {
                        it.put("user_id", userKey)
                    },
                    cookies = session.cookieJar)

    fun blockedAccounts(session: Session) =
            get(url = Endpoints.ACCOUNT_BLOCK_LIST,
                    headers = Crypto.HEADERS,
                    cookies = session.cookieJar)
}
