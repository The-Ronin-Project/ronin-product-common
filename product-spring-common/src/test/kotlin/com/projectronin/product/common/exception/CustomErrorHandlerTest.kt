package com.projectronin.product.common.exception

import com.projectronin.product.common.exception.response.ErrorResponse
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Multiple tests that exception get converted to expected error response body
 *
 * NOTE: most of the 'input error messages' are based on real SpringBoot behavior
 *   The exceptions are meant to simulate how the real exceptions would be structured.
 */
class CustomErrorHandlerTest {

    companion object {
        private val customErrorHandler = CustomErrorHandler()

        private val EXPECTED_BAD_REQUEST_STATUS = HttpStatus.BAD_REQUEST
        private val EXPECTED_UNAUTHORIZED_STATUS = HttpStatus.UNAUTHORIZED
        private val EXPECTED_INTERNAL_ERROR_STATUS = HttpStatus.INTERNAL_SERVER_ERROR
    }

    @Nested
    @DisplayName("Bad Request Exceptions")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class BadRequestExceptions {

        // Constraint Violations are _usually_ missing required fields or size too big
        @Test
        fun `test constraint exception response`() {
            val fieldName = "createAudit.audit.resourceId"
            val inputMessage = "must not be blank"
            val exception = BadRequestExceptionGeneratorUtil().createConstraintViolationException(fieldName, inputMessage)

            val expectedFriendlyMessage = getExpectedFriendlyMissingMessage("resourceId")
            validateException(exception, EXPECTED_BAD_REQUEST_STATUS, expectedFriendlyMessage)
        }

        // alternate validation exception when "@Valid" annotation is used.
        @Test
        fun `test method argument binding exception response`() {
            val fieldName = "resourceId"
            val inputMessage = "size must be between 0 and 255"
            val exception = BadRequestExceptionGeneratorUtil().createMethodArgumentNotValidException(fieldName, inputMessage)

            val expectedFriendlyMessage = getExpectedFriendlyInvalidMessage(fieldName)
            validateException(exception, EXPECTED_BAD_REQUEST_STATUS, expectedFriendlyMessage)
        }

        // missing kotlin field exception - common for case where there is a missing field and
        //   i.e. trying to marshal a request body to an object where the field is declared not-nullable
        @Test
        fun `test missing kotlin field exception response`() {
            val fieldName = "resourceType"
            val exception = BadRequestExceptionGeneratorUtil().createMissingKotlinParameterException(fieldName)

            val expectedFriendlyMessage = getExpectedFriendlyInvalidMessage(fieldName)
            validateException(exception, EXPECTED_BAD_REQUEST_STATUS, expectedFriendlyMessage)
        }

        // mismatched input exception occurs when trying to set a field to incorrect type.
        //   e.g. attempt to set a map to a boolean value     {"dataMap": true}
        @Test
        fun `test mismatched input exception response`() {
            val fieldName = "dataMap"
            val inputMessage = "Cannot deserialize value of type `java.util.LinkedHashMap<java.lang.String,java.lang.Object>` from Boolean value (token `JsonToken.VALUE_TRUE`)"
            val expectedFriendlyMessage = getExpectedFriendlyInvalidMessage(fieldName)

            val exception = BadRequestExceptionGeneratorUtil().createMismatchedInputException(fieldName, inputMessage)
            validateException(exception, EXPECTED_BAD_REQUEST_STATUS, expectedFriendlyMessage)
        }

        // invalid format exceptions when failed to convert value to a field in object.
        //   most common passing in an "Incorrect" date string     {"reportDate": "bogus2022-02-08T13:21:45-06:00"}
        @Test
        fun `test invalid format exception response`() {
            val fieldName = "reportDate"
            val inputMessage = "Cannot deserialize value of type `java.time.ZonedDateTime` from String \"bogus2022-02-08T13:21:45-06:00\""
            val exception = BadRequestExceptionGeneratorUtil().createInvalidFormatException(fieldName, inputMessage)

            val expectedFriendlyMessage = getExpectedFriendlyInvalidMessage(fieldName)
            validateException(exception, EXPECTED_BAD_REQUEST_STATUS, expectedFriendlyMessage)
        }

        // exception for invalid json
        @Test
        fun `test json parse exception response`() {
            val inputMessage = "Unexpected character ('k' (code 107)): was expecting double-quote to start field name" // a real example message
            val exception = BadRequestExceptionGeneratorUtil().createJsonParseException(inputMessage)

            val expectedFriendlyMessage = "JSON Parse Error"
            validateException(exception, EXPECTED_BAD_REQUEST_STATUS, expectedFriendlyMessage)
        }
    }

    @Nested
    @DisplayName("Internal Exceptions")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class InternalExceptions {

        @Test
        fun `test internal database exception response`() {
            // a real message that occurred when tried to save a record that was missing its PrimaryKey id value.
            val inputMessage = "Database; Field 'id' doesn't have a default value; nested exception is java.sql.SQLException: Field 'id' doesn't have a default value"
            val exception = DataIntegrityViolationException(inputMessage)

            val expectedFriendlyMessage = "Internal Database Error"
            validateException(exception, EXPECTED_INTERNAL_ERROR_STATUS, expectedFriendlyMessage)
        }

        // an internal NullPointerException will not have any message (most likely)
        //  thus test for the no message case.
        @Test
        fun `test npe exception response`() {
            val exception = NullPointerException()

            val expectedMessage = "Internal Error"
            validateException(exception, EXPECTED_INTERNAL_ERROR_STATUS, expectedMessage)
        }
    }

    @Nested
    @DisplayName("Authorization Exceptions")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class AuthExceptions {

        @Test
        fun `auth failure response handling`() {

            val mockRequest = mockk<HttpServletRequest>(relaxed = true)
            val mockResponse = mockk<HttpServletResponse>(relaxed = true)
            val authException = PreAuthenticatedCredentialsNotFoundException("Token value was missing or invalid")

            customErrorHandler.onAuthenticationFailure(mockRequest, mockResponse, authException)
            verify(exactly = 1) { mockResponse.setHeader("Content-Type", "application/json") }
            verify(exactly = 1) { mockResponse.status = EXPECTED_UNAUTHORIZED_STATUS.value() }
        }
    }

    @Test
    fun `test generic error with associated status code`() {
        val exception = Exception("Generic exception")
        val exceptionStatus = HttpStatus.I_AM_A_TEAPOT

        val expectedMessage = "I'm a teapot"

        val response = customErrorHandler.handleExceptionInternal(exception, null, mockk(), exceptionStatus, mockk())
        val responseBody = response.body

        assertTrue(responseBody is ErrorResponse)
        responseBody as ErrorResponse
        assertEquals(expectedMessage, responseBody.message)
        assertEquals(HttpStatus.I_AM_A_TEAPOT, response.statusCode)
    }

    private fun getExpectedFriendlyMissingMessage(fieldName: String): String {
        return "Missing required field '$fieldName'"
    }
    private fun getExpectedFriendlyInvalidMessage(fieldName: String): String {
        return "Invalid value for field '$fieldName'"
    }

    private fun validateException(exception: Exception, expectedStatus: HttpStatus, expectedFriendlyMessage: String) {

        // call error handler...
        val responseEntity = customErrorHandler.defaultHandleException(exception, null)
        val errorResponse = responseEntity.body!!

        // confirm response looks correct for anything 'common' to all exceptions
        assertEquals(expectedStatus, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)

        assertEquals(expectedStatus.value(), errorResponse.status, "mismatch error response 'status'")
        assertEquals(expectedFriendlyMessage, errorResponse.message, "mismatch error response 'message'")
    }
}
