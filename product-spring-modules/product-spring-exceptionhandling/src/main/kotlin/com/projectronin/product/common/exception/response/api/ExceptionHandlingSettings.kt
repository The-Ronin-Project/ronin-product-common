package com.projectronin.product.common.exception.response.api

import org.slf4j.Logger
import org.slf4j.event.Level
import org.springframework.http.HttpStatus

class ExceptionHandlingSettings(
    val returnDetailMessages: Boolean,
    val returnExceptionNames: Boolean,
    val returnStacktraces: Boolean,
    val logLevel4xx: Level,
    val logLevel5xx: Level
) {

    companion object {
        @Volatile
        private var settings: ExceptionHandlingSettings? = null
        val returnDetailMessages: Boolean
            get() = settings?.returnDetailMessages ?: false
        val returnExceptionNames: Boolean
            get() = settings?.returnExceptionNames ?: false
        val returnStacktraces: Boolean
            get() = settings?.returnStacktraces ?: false

        fun exceptionLogFunction(status: HttpStatus, logger: Logger): (String, Throwable) -> Unit {
            return { message, cause ->
                logger.atLevel(levelByStatus(status)).setCause(cause).log(message)
            }
        }

        fun logFunction(status: HttpStatus, logger: Logger): (String) -> Unit {
            return { message ->
                logger.atLevel(levelByStatus(status)).log(message)
            }
        }

        private fun levelByStatus(status: HttpStatus) = when {
            status.is4xxClientError -> settings?.logLevel4xx ?: Level.WARN
            status.is5xxServerError -> settings?.logLevel5xx ?: Level.ERROR
            else -> Level.ERROR
        }
    }

    init {
        ExceptionHandlingSettings.settings = this
    }
}
