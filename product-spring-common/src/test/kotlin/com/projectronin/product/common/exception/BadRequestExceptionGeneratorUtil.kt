package com.projectronin.product.common.exception

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.core.MethodParameter
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import kotlin.reflect.KClass

// "wrapping" indicates if certain exceptions should be nested inside a HttpMessageNotReadableException,
//   to mimic actual internal SpringBoot behavior
//     reference: AbstractJackson2HttpMessageConverter::readJavaType
private const val DEFAULT_WRAP = true

class BadRequestExceptionGeneratorUtil {

    fun createConstraintViolationException(fieldPath: String, errorMessage: String, wrapException: Boolean = DEFAULT_WRAP): Exception {

        // NOTE: real constraint violation can potentially have multiple constraints,
        //   but for test purposes only dealing with single case seems sufficient.
        val mockConstraint = mockk<ConstraintViolation<String>> {
            every { propertyPath } returns PathImpl.createPathFromString(fieldPath)
            every { message } returns errorMessage // keep func parameter name different to avoid confusion here
        }
        return makeFinalException(ConstraintViolationException("constraint error", setOf(mockConstraint)), wrapException)
    }

    /**
     * Create exception to mimic problem via @Valid, which would mean this exception would occur
     *   __instead of__ a ConstraintViolationException, MissingKotlinParameterException, etc ...
     */
    fun createMethodArgumentNotValidException(fieldName: String, message: String, wrapException: Boolean = DEFAULT_WRAP): Exception {

        val mockFieldError = mockk<FieldError> {
            every { defaultMessage } returns message
            every { field } returns fieldName
        }
        val mockBindingResult = mockk<BindingResult> {
            every { fieldErrors } returns listOf(mockFieldError)
        }
        val mockMethodParameter = mockk<MethodParameter>()
        return makeFinalException(MethodArgumentNotValidException(mockMethodParameter, mockBindingResult), wrapException)
    }

    /**
     * Create exception to mimic bogus json input
     */
    fun createJsonParseException(message: String, wrapException: Boolean = DEFAULT_WRAP): Exception {
        return makeFinalException(JsonParseException(null, message), wrapException)
    }

    /**
     * Create exception to mimic missing required parameter
     */
    fun createMissingKotlinParameterException(fieldName: String, wrapException: Boolean = DEFAULT_WRAP): Exception {
        // note: there's only 1 message for a MissingKotlinParameter so just pass in blank for testing  (it will be ignored)
        return createMockJsonMappingException(MissingKotlinParameterException::class, fieldName, "", wrapException)
    }

    /**
     * Create exception to mimic setting parameter of incorrect type
     *   (ex: "dataMap": true)
     */
    fun createMismatchedInputException(fieldName: String, message: String, wrapException: Boolean = DEFAULT_WRAP): Exception {
        return createMockJsonMappingException(MismatchedInputException::class, fieldName, message, wrapException)
    }

    /**
     * Create exception to mimic setting parameter invalid value that cannot be converted
     *   (ex: "reportDate": "bogus2022-02-08T13:21:45-06:00")
     */
    fun createInvalidFormatException(fieldName: String, message: String, wrapException: Boolean = DEFAULT_WRAP): Exception {
        return createMockJsonMappingException(InvalidFormatException::class, fieldName, message, wrapException)
    }

    /**
     * create mock for any type of JsonMappingException
     */
    private fun createMockJsonMappingException(classType: KClass<*>, fieldName: String, message: String, wrapException: Boolean): Exception {

        val mockException = mockkClass(classType) as JsonMappingException
        val mockReference = mockk<JsonMappingException.Reference>()

        every { mockException.path } returns listOf(mockReference)
        every { mockException.originalMessage } returns message
        every { mockReference.fieldName } returns fieldName

        return makeFinalException(mockException, wrapException)
    }

    /**
     *  Simulates existing SpringBoot/Jackson behavior that 'might' wrap an exception
     *    inside another exception
     *  @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
     */
    private fun makeFinalException(ex: Exception, shouldWrap: Boolean): Exception {

        if (shouldWrap && ex is JsonProcessingException) {
            // wrapping JsonProcessingException inside HttpMessageNotReadableException mimics internal SpringBoot behavior
            //   reference: AbstractJackson2HttpMessageConverter::readJavaType
            return HttpMessageNotReadableException("JSON parse error: " + ex.originalMessage, ex)
        }
        return ex
    }
}
