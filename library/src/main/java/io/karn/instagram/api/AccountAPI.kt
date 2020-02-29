package io.karn.instagram.api

import io.karn.instagram.core.Crypto
import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get
import khttp.post

internal object AccountAPI : API() {

    fun accountInfo(session: Session, primaryKey: String) =
            get(url = String.format(Endpoints.ACCOUNT_INFO, primaryKey),
                    headers = getRequestHeaders(session),
                    cookies = session.cookieJar)

    fun feed(session: Session, feedUserKey: String, maxId: String, minTimestamp: String) =
            get(url = String.format(Endpoints.ACCOUNT_FEED, feedUserKey),
                    params = mapOf(
                            "max_id" to maxId,
                            "min_timestamp" to minTimestamp,
                            "rank_token" to session.rankToken,
                            "ranked_content" to "true"
                    ),
                    headers = getRequestHeaders(session),
                    cookies = session.cookieJar)

    fun relationships(session: Session, endpoint: String, userKey: String, maxId: String) =
            get(url = String.format(endpoint, userKey),
                    params = mapOf(
                            "rank_token" to session.rankToken,
                            "max_id" to maxId
                    ),
                    headers = getRequestHeaders(session),
                    cookies = session.cookieJar)

    fun updateRelationship(session: Session, endpoint: String, userKey: String) =
            post(url = String.format(endpoint, userKey),
                    headers = getRequestHeaders(session),
                    data = Crypto.generateAuthenticatedParams(session) {
                        it.put("user_id", userKey)
                    },
                    cookies = session.cookieJar)

    fun blockedAccounts(session: Session) =
            get(url = Endpoints.ACCOUNT_BLOCK_LIST,
                    headers = getRequestHeaders(session),
                    cookies = session.cookieJar)
}
