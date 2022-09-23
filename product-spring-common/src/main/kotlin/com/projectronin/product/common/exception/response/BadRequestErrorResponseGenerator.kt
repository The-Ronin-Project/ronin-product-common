package com.projectronin.product.common.exception.response

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import org.springframework.http.HttpStatus
import org.springframework.validation.BindException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import javax.validation.ConstraintViolationException

internal class BadRequestErrorResponseGenerator : AbstractErrorStatusResponseGenerator(HttpStatus.BAD_REQUEST) {

    override fun getErrorMessageInfo(exception: Throwable): ErrorMessageInfo {

        when (exception) {
            // ConstraintViolationException typically refers to field annotation errors
            //    such as "@field:NotBlank"  or  "@field:Size(max = 255)"
            //    however these can fall in the 'BindException' as well
            is ConstraintViolationException -> {
                val firstConstraintViolation = exception.constraintViolations.first()
                val fieldName = firstConstraintViolation.propertyPath?.toString()?.substringAfterLast('.')
                val detailMessage = firstConstraintViolation.message
                val friendlyMessage = getFriendlyFieldErrorMessage(fieldName.orEmpty(), detailMessage)
                return ErrorMessageInfo(friendlyMessage, detailMessage)
            }

            // BindException typically (but not always) caused when using '@Valid' annotation
            //    example: MethodArgumentNotValidException
            is BindException -> {
                val firstError = exception.fieldErrors.first()
                val detailMessage = firstError.defaultMessage.orEmpty()
                val friendlyMessage = getFriendlyFieldErrorMessage(firstError.field, detailMessage)
                return ErrorMessageInfo(friendlyMessage, detailMessage)
            }

            // JsonMappingException typically when a given field is missing or invalid
            //     example: MissingKotlinParameterException, MismatchedInputException
            is JsonMappingException -> {
                val firstErrorFieldName = exception.path.map { it.fieldName }.first()
                val detailMessage = exception.originalMessage
                val friendlyMessage = getFriendlyFieldErrorMessage(firstErrorFieldName, detailMessage)
                return ErrorMessageInfo(friendlyMessage, detailMessage)
            }

            // JsonProcessingException typically when input JSON is invalid
            is JsonProcessingException -> {
                return ErrorMessageInfo("JSON Parse Error", exception.originalMessage)
            }

            // MethodArgumentTypeMismatchException can be on invalid type on url parameter on controller
            //   such as "@GetMapping("/{id}")"
            is MethodArgumentTypeMismatchException -> {
                val detailMessage = exception.message.orEmpty()
                val friendlyMessage = getFriendlyFieldErrorMessage(exception.name, detailMessage)
                return ErrorMessageInfo(friendlyMessage, detailMessage)
            }

            // Any other exception is a generic invalid request
            else -> {
                return ErrorMessageInfo("Invalid Request", exception.message)
            }
        }
    }

    private fun getFriendlyFieldErrorMessage(fieldName: String, detailMessage: String): String {
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
