package io.karn.instagram.api

import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get


internal object DirectMessagesAPI : API() {

    fun getMessages(session: Session, maxID: String) =
            get(url = Endpoints.DIRECT_MESSAGES,
                    params = mapOf(
                            "persistentBadging" to "true",
                            "use_unified_inbox" to "true"
                    ),
                    headers = getRequestHeaders(session),
                    cookies = session.cookieJar)
}
