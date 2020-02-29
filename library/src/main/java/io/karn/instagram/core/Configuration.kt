package io.karn.instagram.core

import io.karn.instagram.HttpResponse


/**
 * Configuration data class for the library, modify the attributes when initializing the library to change its behaviour
 * and or manage the defaults.
 */
data class Configuration(
        internal var deviceDPI: String = "640dpi",
        internal var deviceResolution: String = "1440x2560",

        /**
         * Attach a logger to process API calls.
         */
        var requestLogger: ((response: HttpResponse) -> Unit)? = null,

        /**
         * Attach a listener for session updates.
         */
        var sessionUpdateListener: ((session: Session) -> Unit)? = null
)
