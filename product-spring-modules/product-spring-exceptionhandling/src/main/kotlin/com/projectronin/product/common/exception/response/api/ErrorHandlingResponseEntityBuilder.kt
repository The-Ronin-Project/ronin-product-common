package com.projectronin.product.common.exception.response.api

import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

/**
 * An interface containing a bundle of default methods meant to make the process of generating the `ErrorResponse` object
 * easier for implementors.  See `SpringErrorHandler` for an example of use.  Especially useful where another superclass
 * is necessary, as in `SpringErrorHandler`.
 */
interface ErrorHandlingResponseEntityBuilder<in T : Throwable> {

    val roninLogger: Logger

    /**
     * Generate an ResponseEntity error response based on the exception
     */
    fun generateResponseEntity(
        exception: T,
        existingStatus: HttpStatus? = null
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = generateErrorResponse(exception, existingStatus)

        return ResponseEntity
            .status(errorResponse.httpStatus)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    /**
     * Create an ErrorResponse based on exception
     * @param exception the exception used for the error response
     * @param existingStatus (optional) hint status to use for errorResponse if statusCode not determined by exception
     * @return the errorResponse object
     */
    fun generateErrorResponse(exception: T, existingStatus: HttpStatus? = null): ErrorResponse {
        val errorResponse = getErrorResponseFromException(exception, existingStatus)

        val loggableErrorMessage =
            "Request error: ${errorResponse.message}, ${errorResponse.detail}, ${errorResponse.exception}"
        if (errorResponse.httpStatus.is5xxServerError) {
            roninLogger.error(loggableErrorMessage, exception)
        } else {
            // don't need to pass in exception for stacktrace for these types of client errors
            roninLogger.warn(loggableErrorMessage)
        }
        return errorResponse
    }

    /**
     * Converts an exception into an error response
     *
     * @param exception The thrown exception
     * @param existingHttpStatus The status, if any, that was returned with the exception
     */
    fun getErrorResponseFromException(exception: T, existingHttpStatus: HttpStatus?): ErrorResponse
}
