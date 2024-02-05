package com.projectronin.product.common.exception.advice

import com.projectronin.product.common.exception.response.api.AbstractErrorHandlingEntityBuilder
import com.projectronin.product.common.exception.response.api.ErrorHandlingResponseEntityConstructor
import com.projectronin.product.common.exception.response.api.ErrorResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * A controller advice that provides a default exception handler for any unhandled exception.  First checks to see
 * if the exception is annotated with `ResponseStatus` and uses that status and reason if present.  Defaults
 * to the existing http status if there is one, and returns 500 if not.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class InternalErrorHandler(@Autowired override val responseEntityConstructor: ErrorHandlingResponseEntityConstructor) : AbstractErrorHandlingEntityBuilder<Throwable>() {

    //  this is a catch-all for any exception types not already handled.
    @ExceptionHandler
    @ResponseBody
    fun defaultHandleException(exception: Throwable): ResponseEntity<Any> {
        return generateResponseEntity(exception)
    }

    override fun buildErrorResponse(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorResponse {
        // HttpStatus.INTERNAL_SERVER_ERROR
        val (httpStatus, reasonPhrase) = getHttpStatus(exception, existingHttpStatus)
        return ErrorResponse.logAndCreateErrorResponse(
            logger = roninLogger,
            httpStatus = httpStatus,
            exception = exception,
            message = reasonPhrase,
            detail = exception.message
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
