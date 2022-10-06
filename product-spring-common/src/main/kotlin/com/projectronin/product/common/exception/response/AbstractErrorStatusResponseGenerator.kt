package com.projectronin.product.common.exception.response

import org.springframework.http.HttpStatus

/**
 * An abstract convenience implementation for ErrorStatusResponseGenerator which accepts an http status
 * and delegates the message and detail fields to `getErrorMessageInfo`.
 */
abstract class AbstractErrorStatusResponseGenerator(protected val defaultHttpStatus: HttpStatus) :
    ErrorStatusResponseGenerator {

    // simple container to hold error message strings
    protected data class ErrorMessageInfo(val message: String, val detail: String?)

    // child classes must implement this method for error string handling
    protected abstract fun getErrorMessageInfo(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorMessageInfo?

    /**
     * Override if your subclass needs to perform some logic to determine the right status.
     */
    protected open fun getHttpStatus(exception: Throwable, existingHttpStatus: HttpStatus?): HttpStatus = defaultHttpStatus

    /**
     * {@inheritDoc}
     */
    override fun buildErrorResponse(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorResponse? {
        return when (val errorMessageInfo = getErrorMessageInfo(exception, existingHttpStatus)) {
            null -> null
            else -> {
                val finalStatus = getHttpStatus(exception, existingHttpStatus)

                ErrorResponse(
                    httpStatus = finalStatus,
                    exception = exception.getExceptionName(),
                    message = errorMessageInfo.message,
                    detail = errorMessageInfo.detail,
                    stacktrace = optionallyGetStackTrace(finalStatus, exception),
                )
            }
        }
    }
}
