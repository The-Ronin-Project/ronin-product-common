package com.projectronin.product.common.exception.advice

import com.projectronin.product.common.exception.response.api.AbstractErrorHandlingEntityBuilder
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.exception.response.api.getExceptionName
import com.projectronin.product.common.exception.response.api.optionallyGetStackTrace
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest

/**
 * A controller advice that provides a default exception handler for any unhandled exception.  First checks to see
 * if the exception is annotated with `ResponseStatus` and uses that status and reason if present.  Defaults
 * to the existing http status if there is one, and returns 500 if not.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class InternalErrorHandler : AbstractErrorHandlingEntityBuilder<Throwable>() {

    //  this is a catch-all for any exception types not already handled.
    @ExceptionHandler
    @ResponseBody
    fun defaultHandleException(exception: Throwable, request: WebRequest?): ResponseEntity<ErrorResponse> {
        return generateResponseEntity(exception)
    }

    override fun buildErrorResponse(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorResponse {
        // HttpStatus.INTERNAL_SERVER_ERROR
        val (httpStatus, reasonPhrase) = getHttpStatus(exception, existingHttpStatus)
        return ErrorResponse(
            httpStatus = httpStatus,
            exception = exception.getExceptionName(),
            message = reasonPhrase,
            detail = exception.message,
            stacktrace = optionallyGetStackTrace(httpStatus, exception),
        )
    }

    private fun getHttpStatus(exception: Throwable, existingHttpStatus: HttpStatus?): Pair<HttpStatus, String> {
        return when (
            val foundAnnotation = AnnotationUtils.findAnnotation(exception.javaClass, ResponseStatus::class.java)
        ) {
            null -> {
                val code = existingHttpStatus ?: HttpStatus.INTERNAL_SERVER_ERROR
                code to code.reasonPhrase
            }

            else -> foundAnnotation.code to foundAnnotation.reason.ifBlank { foundAnnotation.code.reasonPhrase }
        }
    }
}
