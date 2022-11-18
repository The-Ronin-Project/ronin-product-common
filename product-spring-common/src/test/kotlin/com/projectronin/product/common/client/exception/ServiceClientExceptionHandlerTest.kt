package com.projectronin.product.common.client.exception

import com.projectronin.product.common.client.ServiceResponse
import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.exception.response.api.getExceptionName
import com.projectronin.validation.clinical.data.client.work.exception.ServiceClientException
import com.projectronin.validation.clinical.data.client.work.exception.ServiceClientExceptionHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.net.UnknownHostException

class ServiceClientExceptionHandlerTest {

    @Test
    fun `contruct exception from error response happy path`() {

        // errorResponse to be converted to a string to represent the error response string message.
        val errResponse = ErrorResponse(
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            exception = RuntimeException().getExceptionName(),
            message = "something bad",
            detail = "a really really bad thing occurred",
            stacktrace = ""
        )
        val serviceResponse = generateErrorServiceResponse(errResponse)

        val exception = assertThrows<ServiceClientException> {
            ServiceClientExceptionHandler().handleError(serviceResponse)
        }

        // note that errResponse is not a data class (to allow it to be potentially extended 'just in case')
        assertEquals(errResponse.httpStatus, exception.errorResponse?.httpStatus, "mismatch exception httpStatus")
        assertEquals(errResponse.message, exception.errorResponse?.message, "mismatch exception nested message")
    }

    @Test
    fun `unrecognized error response format`() {
        val errorCode = 503
        val errorResponseBody = "unparsable response body"
        val serviceResponse = ServiceResponse(errorCode, errorResponseBody)

        val exception = assertThrows<ServiceClientException> {
            ServiceClientExceptionHandler().handleError(serviceResponse)
        }

        assertEquals(errorCode, exception.getHttpStatusCode(), "mismatch exception httpStatus")
        assertEquals(errorResponseBody, exception.errorResponse?.detail, "mismatch exception nested detail")
    }

    // testcase for a normal exception (instead of a 4xx/5xx httpStatus response)
    @Test
    fun `general exception`() {

        val errorMsg = "Unknown host: foo.bar.com"
        val ex = UnknownHostException(errorMsg)

        val exception = assertThrows<ServiceClientException> {
            ServiceClientExceptionHandler().handleException(ex)
        }

        assertEquals(0, exception.getHttpStatusCode(), "expected 0 for statusCode ehen no httpStatus available")
        assertEquals(ex.javaClass, exception.cause?.javaClass, "mismatch exception nested cause class")
        assertEquals(ex.message, exception.cause?.message, "mismatch exception nested cause message")
        assertTrue(exception.message!!.contains(errorMsg), "expected exception message '' to contain substring '${errorMsg}'")
    }

    private fun generateErrorServiceResponse(errResponse: ErrorResponse): ServiceResponse {
        val errResponseString = JsonProvider.objectMapper.writeValueAsString(errResponse)
        return ServiceResponse(errResponse.status, errResponseString)
    }
}
