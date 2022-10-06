package com.projectronin.product.common.exception.response

import org.springframework.core.Ordered
import org.springframework.http.HttpStatus

/**
 * Used as a "catch-all" response generator if no other generator in the chain returns a value.
 */
internal object InternalErrorResponseGenerator : ErrorStatusResponseGenerator {

    override fun buildErrorResponse(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorResponse {
        return ErrorResponse(
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            exception = exception.getExceptionName(),
            message = "Internal Error",
            detail = exception.message,
            stacktrace = exception.stackTraceToString(),
        )
    }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE
}
