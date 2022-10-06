package com.projectronin.product.common.exception.response

import org.springframework.core.Ordered
import org.springframework.http.HttpStatus

/**
 * Optionally handles an exception + status from `CustomErrorHandler`.  `CustomErrorHandler` accepts
 * a list of these, ordered by the `Ordered` interface.  Lowest-ordered generator that returns non-null
 * wins.
 *
 * @see com.projectronin.product.common.exception.CustomErrorHandler
 */
interface ErrorStatusResponseGenerator : Ordered {
    /**
     * Build an error response object for any given exception.  Return null to reject handling of this
     * exception.
     */
    fun buildErrorResponse(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorResponse?
}
