package com.projectronin.product.common.exception

import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.advice.SpringErrorHandler
import com.projectronin.product.common.exception.auth.CustomAuthenticationFailureHandler
import com.projectronin.product.common.exception.response.api.ErrorResponse
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException

/**
 * Multiple tests that exception get converted to expected error response body
 *
 * NOTE: most of the 'input error messages' are based on real SpringBoot behavior
 *   The exceptions are meant to simulate how the real exceptions would be structured.
 */
class CustomErrorHandlerTest {

    companion object {
        private val customErrorHandler = SpringErrorHandler()

        private val EXPECTED_UNAUTHORIZED_STATUS = HttpStatus.UNAUTHORIZED
        private val EXPECTED_INTERNAL_ERROR_STATUS = HttpStatus.INTERNAL_SERVER_ERROR
    }

    @Nested
    @DisplayName("Internal Exceptions")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class InternalExceptions {

        // an internal NullPointerException will not have any message (most likely)
        //  thus test for the no message case.
        @Test
        fun `test npe exception response`() {
            val exception = NullPointerException()

            val expectedMessage = "Internal Server Error"
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

            CustomAuthenticationFailureHandler(JsonProvider.objectMapper).onAuthenticationFailure(mockRequest, mockResponse, authException)
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
        val responseEntity = customErrorHandler.generateResponseEntity(exception, null)
        val errorResponse = responseEntity.body!!

        // confirm response looks correct for anything 'common' to all exceptions
        assertEquals(expectedStatus, responseEntity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.headers.contentType)

        assertEquals(expectedStatus.value(), errorResponse.status, "mismatch error response 'status'")
        assertEquals(expectedFriendlyMessage, errorResponse.message, "mismatch error response 'message'")
    }
}
