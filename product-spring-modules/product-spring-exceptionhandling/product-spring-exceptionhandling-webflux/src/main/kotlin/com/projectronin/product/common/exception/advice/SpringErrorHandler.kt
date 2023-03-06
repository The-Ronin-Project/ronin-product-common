package com.projectronin.product.common.exception.advice

import com.projectronin.product.common.exception.response.api.ErrorHandlingResponseEntityBuilder
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.exception.response.api.getExceptionName
import com.projectronin.product.common.exception.response.api.optionallyGetStackTrace
import org.apache.commons.logging.Log
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Extends `ResponseEntityExceptionHandler` to provide our custom `ErrorResponse` body for all internal spring
 * exceptions handled by that class.  It accepts the HttpStatus generated for all those exceptions by that class,
 * and only customizes the response body.  Must remain at a higher precedence than the "catch-all" handler.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
class SpringErrorHandler : ResponseEntityExceptionHandler(), ErrorHandlingResponseEntityBuilder<Throwable> {

    override fun handleExceptionInternal(
        ex: java.lang.Exception,
        body: Any?,
        headers: HttpHeaders?,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        @Suppress("UNCHECKED_CAST")
        return Mono.just(generateResponseEntity(ex, HttpStatus.valueOf(status.value())) as ResponseEntity<Any>)
    }

    override val logger: Log
        get() = super.logger

    /**
     * Converts an exception into an error response
     *
     * @param exception The thrown exception
     * @param existingHttpStatus The status, if any, that was returned with the exception
     */
    override fun getErrorResponseFromException(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorResponse {
        val status = existingHttpStatus ?: HttpStatus.INTERNAL_SERVER_ERROR
        return ErrorResponse(
            httpStatus = status,
            exception = exception.getExceptionName(),
            message = status.reasonPhrase,
            detail = exception.message,
            stacktrace = optionallyGetStackTrace(status, exception)
        )
    }
}
