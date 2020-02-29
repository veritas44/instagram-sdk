package io.karn.instagram.api

import io.karn.instagram.core.Endpoints
import io.karn.instagram.core.Session
import khttp.get

internal object CollectionsAPI : API() {

    fun listCollections(session: Session) =
            get(url = Endpoints.COLLECTIONS_LIST,
                    headers = getRequestHeaders(session),
                    cookies = session.cookieJar)

    fun getCollection(session: Session, collectionId: String) =
            get(url = "${Endpoints.COLLECTIONS_LIST}$collectionId",
                    headers = getRequestHeaders(session),
                    cookies = session.cookieJar)
}
