package com.projectronin.product.common.exception.response.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.projectronin.common.telemetry.addToDDTraceSpan
import datadog.trace.api.CorrelationIdentifier
import org.slf4j.Logger
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID

/**
 * Intended to be the single basic response that we return on any exception.  It would be acceptable to return
 * a subclass, if (for instance) a list of failed form fields needs to be included or similar.
 *
 * Note that "Null" values will NOT be in the marshalled response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
// source: Name of the service that threw the exception. MUST be supplied and used in case the error came somewhere mid call chain between services. If wrapping exception from external system, must be done at the point of calling the external system.
open class ErrorResponse private constructor(
    val id: String,
    val httpStatus: HttpStatus,
    val timestamp: Instant? = Instant.now(),
    val status: Int = httpStatus.value(),
    val error: String? = httpStatus.reasonPhrase,
    val exception: String,
    val message: String,
    val detail: String? = null,
    val stacktrace: String? = null
) {
    companion object {
        fun logAndCreateErrorResponse(
            logger: Logger,
            httpStatus: HttpStatus,
            timestamp: Instant? = Instant.now(),
            status: Int = httpStatus.value(),
            error: String? = httpStatus.reasonPhrase,
            message: String,
            exception: Throwable? = null,
            detail: String? = null
        ): ErrorResponse {
            val id = CorrelationIdentifier.getTraceId()?.takeIf { it != "0" } ?: UUID.randomUUID().toString()
            val errorMessage = "Responding with status $httpStatus for id $id, $error/$message/$detail"
            MDC.putCloseable("errorId", id).use {
                if (exception != null) {
                    exception.addToDDTraceSpan(exceptionUniqueId = id)
                    ExceptionHandlingSettings.exceptionLogFunction(httpStatus, logger)(errorMessage, exception)
                } else {
                    ExceptionHandlingSettings.logFunction(httpStatus, logger)(errorMessage)
                }
            }
            return ErrorResponse(
                id = id,
                httpStatus = httpStatus,
                timestamp = timestamp,
                status = status,
                error = error,
                message = message,
                exception = exception?.getExceptionName()?.takeIf { ExceptionHandlingSettings.returnExceptionNames } ?: "Exception",
                detail = (detail ?: exception?.message)?.takeIf { ExceptionHandlingSettings.returnDetailMessages },
                stacktrace = exception?.stackTraceToString()?.takeIf { ExceptionHandlingSettings.returnStacktraces }
            )
        }
    }
}
