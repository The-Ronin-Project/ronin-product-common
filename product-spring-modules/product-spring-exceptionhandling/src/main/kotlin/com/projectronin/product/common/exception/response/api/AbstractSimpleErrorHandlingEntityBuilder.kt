package com.projectronin.product.common.exception.response.api

import org.springframework.http.HttpStatus

/**
 * Further extension of AbstractErrorHandlingEntityBuilder that generates the response and only delegates the
 * actual error message to subclasses.  Meant for cases where the http status is known, and the only
 * variance in subclasses is the actual error messaging.  See `BadRequestErrorResponseGenerator` and subclasses
 * for examples.
 */
abstract class AbstractSimpleErrorHandlingEntityBuilder<in T : Throwable>(private val httpStatus: HttpStatus) :
    AbstractErrorHandlingEntityBuilder<T>() {

    protected abstract fun getErrorMessageInfo(exception: T, existingHttpStatus: HttpStatus?): ErrorMessageInfo

    /**
     * {@inheritDoc}
     */
    override fun buildErrorResponse(exception: T, existingHttpStatus: HttpStatus?): ErrorResponse {
        val errorMessageInfo = getErrorMessageInfo(exception, existingHttpStatus)
        return ErrorResponse.logAndCreateErrorResponse(
            logger = roninLogger,
            httpStatus = httpStatus,
            exception = exception,
            message = errorMessageInfo.message,
            detail = errorMessageInfo.detail
        )
    }
}
