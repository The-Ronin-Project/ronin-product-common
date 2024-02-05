package com.projectronin.product.common.exception.advice

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.projectronin.product.common.exception.response.api.AbstractSimpleErrorHandlingEntityBuilder
import com.projectronin.product.common.exception.response.api.ErrorHandlingResponseEntityConstructor
import com.projectronin.product.common.exception.response.api.ErrorMessageInfo
import jakarta.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ServerWebInputException

internal fun getFriendlyFieldErrorMessage(fieldName: String, detailMessage: String): String {
    return if (detailMessage.contains("must not be empty") ||
        detailMessage.contains("must not be blank") ||
        detailMessage.contains("non-nullable")
    ) {
        missingFieldFriendlyMessage(fieldName)
    } else {
        invalidFieldFriendlyMessage(fieldName)
    }
}

internal fun missingFieldFriendlyMessage(fieldName: String): String {
    return "Missing required field '$fieldName'"
}

internal fun invalidFieldFriendlyMessage(fieldName: String): String {
    return "Invalid value for field '$fieldName'"
}

/**
 * A collection of "400: bad request" exception handlers.
 */
abstract class BadRequestErrorResponseGenerator<in T : Throwable> :
    AbstractSimpleErrorHandlingEntityBuilder<T>(HttpStatus.BAD_REQUEST)

/**
 * These seem to be raised primarily in the case of custom validator use, but in theory could also be raised on validated
 * parameters other than the body.
 */
@ControllerAdvice
@Order(0)
class ConstraintViolationExceptionResponseGenerator(@Autowired override val responseEntityConstructor: ErrorHandlingResponseEntityConstructor) : BadRequestErrorResponseGenerator<ConstraintViolationException>() {

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseBody
    fun handleConstraintViolationException(
        exception: ConstraintViolationException
    ): ResponseEntity<Any> {
        return generateResponseEntity(exception)
    }

    override fun getErrorMessageInfo(
        exception: ConstraintViolationException,
        existingHttpStatus: HttpStatus?
    ): ErrorMessageInfo {
        val firstConstraintViolation = exception.constraintViolations.first()
        val fieldName = firstConstraintViolation.propertyPath?.toString()?.substringAfterLast('.')
        val detailMessage = firstConstraintViolation.message
        val friendlyMessage = getFriendlyFieldErrorMessage(fieldName.orEmpty(), detailMessage)
        return ErrorMessageInfo(friendlyMessage, detailMessage)
    }
}

/**
 * BindException typically (but not always) caused when using '@Valid' annotation
 *    example: MethodArgumentNotValidException
 */
@ControllerAdvice
@Order(0)
class BindExceptionResponseGenerator(@Autowired override val responseEntityConstructor: ErrorHandlingResponseEntityConstructor) : BadRequestErrorResponseGenerator<BindException>() {
    @ExceptionHandler(BindException::class)
    @ResponseBody
    fun handleBindException(exception: BindException): ResponseEntity<Any> {
        return generateResponseEntity(exception)
    }

    override fun getErrorMessageInfo(exception: BindException, existingHttpStatus: HttpStatus?): ErrorMessageInfo {
        val firstError = exception.fieldErrors.first()
        val detailMessage = firstError.defaultMessage.orEmpty()
        val friendlyMessage = getFriendlyFieldErrorMessage(firstError.field, detailMessage)
        return ErrorMessageInfo(friendlyMessage, detailMessage)
    }
}

/**
 * JsonProcessingException typically when input JSON is invalid
 */
@ControllerAdvice
@Order(0)
class JsonProcessingExceptionResponseGenerator(@Autowired override val responseEntityConstructor: ErrorHandlingResponseEntityConstructor) : BadRequestErrorResponseGenerator<JsonProcessingException>() {
    @ExceptionHandler(JsonProcessingException::class)
    @ResponseBody
    fun handleJsonProcessingException(
        exception: JsonProcessingException
    ): ResponseEntity<Any> {
        return generateResponseEntity(exception)
    }

    override fun getErrorMessageInfo(
        exception: JsonProcessingException,
        existingHttpStatus: HttpStatus?
    ): ErrorMessageInfo {
        return ErrorMessageInfo("JSON Parse Error", exception.originalMessage)
    }
}

/**
 * JsonMappingException typically when a given field is missing or invalid
 *     example: MissingKotlinParameterException, MismatchedInputException
 */
@ControllerAdvice
@Order(-10) // set higher than JsonProcessingExceptionResponseGenerator because JsonMappingException _is a_ JsonProcessingException, and order seems unpredictable
class JsonMappingExceptionResponseGenerator(@Autowired override val responseEntityConstructor: ErrorHandlingResponseEntityConstructor) : BadRequestErrorResponseGenerator<JsonMappingException>() {
    @ExceptionHandler(JsonMappingException::class)
    @ResponseBody
    fun handleJsonMappingException(
        exception: JsonMappingException
    ): ResponseEntity<Any> {
        return generateResponseEntity(exception)
    }

    override fun getErrorMessageInfo(
        exception: JsonMappingException,
        existingHttpStatus: HttpStatus?
    ): ErrorMessageInfo {
        val firstErrorFieldName = exception.path.map { it.fieldName }.first()
        val detailMessage = exception.originalMessage
        val friendlyMessage = getFriendlyFieldErrorMessage(firstErrorFieldName, detailMessage)
        return ErrorMessageInfo(friendlyMessage, detailMessage)
    }
}

/**
 * MethodArgumentTypeMismatchException can be on invalid type on url parameter on controller
 *   such as "@GetMapping("/{id}")"
 */
@ControllerAdvice
@Order(0)
class MethodArgumentTypeMismatchExceptionResponseGenerator(@Autowired override val responseEntityConstructor: ErrorHandlingResponseEntityConstructor) :
    BadRequestErrorResponseGenerator<MethodArgumentTypeMismatchException>() {
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseBody
    fun handleMethodTypeArgumentTypeMismatchException(
        exception: MethodArgumentTypeMismatchException
    ): ResponseEntity<Any> {
        //  this is a catch-all for any exception types not already handled.
        return generateResponseEntity(exception)
    }

    override fun getErrorMessageInfo(
        exception: MethodArgumentTypeMismatchException,
        existingHttpStatus: HttpStatus?
    ): ErrorMessageInfo {
        val detailMessage = exception.message.orEmpty()
        val friendlyMessage = getFriendlyFieldErrorMessage(exception.name, detailMessage)
        return ErrorMessageInfo(friendlyMessage, detailMessage)
    }
}

@ControllerAdvice
@Order(0)
class ServerWebInputExceptionResponseGenerator(@Autowired override val responseEntityConstructor: ErrorHandlingResponseEntityConstructor) :
    BadRequestErrorResponseGenerator<ServerWebInputException>() {
    @ExceptionHandler(ServerWebInputException::class)
    @ResponseBody
    fun handleMethodTypeArgumentTypeMismatchException(
        exception: ServerWebInputException
    ): ResponseEntity<Any> {
        //  this is a catch-all for any exception types not already handled.
        return generateResponseEntity(exception)
    }

    override fun getErrorMessageInfo(
        exception: ServerWebInputException,
        existingHttpStatus: HttpStatus?
    ): ErrorMessageInfo {
        return when (exception) {
            is WebExchangeBindException -> {
                val detailMessage = exception.allErrors.joinToString(",", "[", "]")
                ErrorMessageInfo(exception.reason.toString(), detailMessage)
            }
            else -> {
                val detailMessage = exception.reason ?: "unknown"
                val friendlyMessage = getFriendlyFieldErrorMessage(exception.methodParameter?.parameterName ?: "unknown", detailMessage)
                ErrorMessageInfo(friendlyMessage, detailMessage)
            }
        }
    }
}
