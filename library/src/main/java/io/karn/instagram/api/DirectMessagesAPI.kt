package io.karn.instagram.api

import io.karn.instagram.core.Crypto
import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get


internal object DirectMessagesAPI {

    fun getMessages(maxID: String, session: Session) =
            get(url = Endpoints.DIRECT_MESSAGES,
                    params = mapOf(
                            "persistentBadging" to "true",
                            "use_unified_inbox" to "true"
                    ),
                    headers = Crypto.HEADERS,
                    cookies = session.cookieJar)
}
