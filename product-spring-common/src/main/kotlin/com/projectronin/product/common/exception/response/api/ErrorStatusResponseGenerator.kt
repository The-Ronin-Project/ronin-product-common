package com.projectronin.product.common.exception.response.api

import org.springframework.http.HttpStatus

/**
 * Builds an error response from an exception and optional current response status.
 */
interface ErrorStatusResponseGenerator<in T : Throwable> {
    /**
     * Build an error response object for any given exception.  Must return a response.
     */
    fun buildErrorResponse(exception: T, existingHttpStatus: HttpStatus?): ErrorResponse
}
