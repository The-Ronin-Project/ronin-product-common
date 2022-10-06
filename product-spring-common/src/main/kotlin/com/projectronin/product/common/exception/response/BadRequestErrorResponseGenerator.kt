package com.projectronin.product.common.exception.response

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.validation.BindException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import javax.validation.ConstraintViolationException

internal abstract class BadRequestErrorResponseGenerator :
    AbstractErrorStatusResponseGenerator(HttpStatus.BAD_REQUEST) {

    override fun getOrder(): Int = 0

    protected fun getFriendlyFieldErrorMessage(fieldName: String, detailMessage: String): String {
        return if (detailMessage.contains("must not be empty") ||
            detailMessage.contains("must not be blank") ||
            detailMessage.contains("non-nullable")
        ) {
            missingFieldFriendlyMessage(fieldName)
        } else {
            invalidFieldFriendlyMessage(fieldName)
        }
    }

    private fun missingFieldFriendlyMessage(fieldName: String): String {
        return "Missing required field '$fieldName'"
    }

    private fun invalidFieldFriendlyMessage(fieldName: String): String {
        return "Invalid value for field '$fieldName'"
    }
}

@Component
internal class ConstraintViolationExceptionResponseGenerator : BadRequestErrorResponseGenerator() {
    override fun getErrorMessageInfo(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorMessageInfo? =
        when (exception) {
            is ConstraintViolationException -> {
                val firstConstraintViolation = exception.constraintViolations.first()
                val fieldName = firstConstraintViolation.propertyPath?.toString()?.substringAfterLast('.')
                val detailMessage = firstConstraintViolation.message
                val friendlyMessage = getFriendlyFieldErrorMessage(fieldName.orEmpty(), detailMessage)
                ErrorMessageInfo(friendlyMessage, detailMessage)
            }

            else -> null
        }
}

// BindException typically (but not always) caused when using '@Valid' annotation
//    example: MethodArgumentNotValidException
@Component
internal class BindExceptionResponseGenerator : BadRequestErrorResponseGenerator() {
    override fun getErrorMessageInfo(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorMessageInfo? =
        when (exception) {
            is BindException -> {
                val firstError = exception.fieldErrors.first()
                val detailMessage = firstError.defaultMessage.orEmpty()
                val friendlyMessage = getFriendlyFieldErrorMessage(firstError.field, detailMessage)
                ErrorMessageInfo(friendlyMessage, detailMessage)
            }

            else -> null
        }
}

// JsonMappingException typically when a given field is missing or invalid
//     example: MissingKotlinParameterException, MismatchedInputException
// JsonProcessingException typically when input JSON is invalid
@Component
internal class JsonProcessingExceptionResponseGenerator : BadRequestErrorResponseGenerator() {
    override fun getErrorMessageInfo(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorMessageInfo? =
        when (exception) {
            is JsonMappingException -> {
                val firstErrorFieldName = exception.path.map { it.fieldName }.first()
                val detailMessage = exception.originalMessage
                val friendlyMessage = getFriendlyFieldErrorMessage(firstErrorFieldName, detailMessage)
                ErrorMessageInfo(friendlyMessage, detailMessage)
            }
            is JsonProcessingException -> {
                ErrorMessageInfo("JSON Parse Error", exception.originalMessage)
            }

            else -> null
        }
}

// MethodArgumentTypeMismatchException can be on invalid type on url parameter on controller
//   such as "@GetMapping("/{id}")"
@Component
internal class MethodArgumentTypeMismatchExceptionResponseGenerator : BadRequestErrorResponseGenerator() {
    override fun getErrorMessageInfo(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorMessageInfo? =
        when (exception) {
            is MethodArgumentTypeMismatchException -> {
                val detailMessage = exception.message.orEmpty()
                val friendlyMessage = getFriendlyFieldErrorMessage(exception.name, detailMessage)
                ErrorMessageInfo(friendlyMessage, detailMessage)
            }

            else -> null
        }
}
