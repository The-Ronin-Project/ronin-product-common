package com.projectronin.product.common.exception.response

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal abstract class AbstractErrorStatusResponseGenerator(protected val httpStatus: HttpStatus) :
    ErrorStatusResponseGenerator {

    // simple container to hold error message strings
    protected data class ErrorMessageInfo(val message: String, val detail: String?)

    // child classes must implement this method for error string handling
    protected abstract fun getErrorMessageInfo(exception: Throwable): ErrorMessageInfo

    /**
     * {@inheritDoc}
     */
    override fun buildErrorResponse(exception: Throwable): ErrorResponse {

        val errorMessageInfo = getErrorMessageInfo(exception)

        val errorResponse = ErrorResponse(httpStatus).apply {
            status = httpStatus.value()
            timestamp = getTimestampString()
            error = httpStatus.reasonPhrase
            this.exception = getExceptionName(exception) // todo might remove or change
            message = errorMessageInfo.message
            detail = errorMessageInfo.detail

            if (includeStackTrace()) {
                stacktrace = generateStackTrace(exception)
            }
        }

        return errorResponse
    }

    /**
     * Get string representation of exception for error message.
     */
    private fun getExceptionName(exception: Throwable): String {
        return exception.javaClass.name
    }

    /**
     * String representation of the cCURRENT timestamp
     */
    private fun getTimestampString(): String {
        return DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now())
    }

    /**
     * flag to include stacktrace as part of error response.
     *   default is return stacktrace ONLY for 5xx exceptions
     */
    protected open fun includeStackTrace(): Boolean {
        return httpStatus.is5xxServerError
    }

    private fun generateStackTrace(exception: Throwable): String {
        return exception.stackTraceToString()
    }
}
