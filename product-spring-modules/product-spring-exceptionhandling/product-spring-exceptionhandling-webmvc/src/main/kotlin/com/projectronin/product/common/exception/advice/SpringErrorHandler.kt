package com.projectronin.product.common.exception.advice

import com.projectronin.product.common.exception.response.api.ErrorHandlingResponseEntityBuilder
import com.projectronin.product.common.exception.response.api.ErrorResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Extends `ResponseEntityExceptionHandler` to provide our custom `ErrorResponse` body for all internal spring
 * exceptions handled by that class.  It accepts the HttpStatus generated for all those exceptions by that class,
 * and only customizes the response body.  Must remain at a higher precedence than the "catch-all" handler.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
class SpringErrorHandler : ResponseEntityExceptionHandler(), ErrorHandlingResponseEntityBuilder<Throwable> {

    /**
     * {@inheritDoc}
     * Default handling for common client exceptions
     */
    public override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        // the parent ResponseEntityExceptionHandler has already figured out
        //    the correct statusCode for many (_but not all_) exceptions
        //    (this is useful for errors like 405, 406, 415, etc)
        @Suppress("UNCHECKED_CAST")
        return generateResponseEntity(ex, HttpStatus.valueOf(statusCode.value())) as ResponseEntity<Any>
    }

    override val roninLogger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Converts an exception into an error response
     *
     * @param exception The thrown exception
     * @param existingHttpStatus The status, if any, that was returned with the exception
     */
    override fun getErrorResponseFromException(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorResponse {
        val status = existingHttpStatus ?: HttpStatus.INTERNAL_SERVER_ERROR
        return ErrorResponse.logAndCreateErrorResponse(
            logger = roninLogger,
            httpStatus = status,
            exception = exception,
            message = status.reasonPhrase,
            detail = exception.message
        )
    }
}
