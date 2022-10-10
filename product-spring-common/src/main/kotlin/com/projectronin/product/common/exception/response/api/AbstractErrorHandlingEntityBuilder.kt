package com.projectronin.product.common.exception.response.api

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpStatus

/**
 * Abstract superclass that implements the major interfaces in this package.  A convenience for implementors.  See
 * `InternalErrorHandler` for an example of use.  Meant fo cases when the status response might be variable in ways that
 * a simple message isn't enough to account for.
 */
abstract class AbstractErrorHandlingEntityBuilder<in T : Throwable> : ErrorHandlingResponseEntityBuilder<T>, ErrorStatusResponseGenerator<T> {

    override val logger: Log
        get() = LogFactory.getLog(javaClass)

    override fun getErrorResponseFromException(exception: T, existingHttpStatus: HttpStatus?): ErrorResponse {
        return buildErrorResponse(exception, existingHttpStatus)
    }
}
