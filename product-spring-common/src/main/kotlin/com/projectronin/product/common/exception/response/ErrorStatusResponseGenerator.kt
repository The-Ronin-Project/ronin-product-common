package com.projectronin.product.common.exception.response

interface ErrorStatusResponseGenerator {

    /**
     * Build an error response object for any given exception
     */
    fun buildErrorResponse(exception: Throwable): ErrorResponse
}
