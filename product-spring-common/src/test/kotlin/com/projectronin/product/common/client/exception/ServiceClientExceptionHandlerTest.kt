package com.projectronin.product.common.client.exception

import com.projectronin.product.common.client.ServiceResponse
import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.exception.response.api.getExceptionName
import com.projectronin.validation.clinical.data.client.work.exception.ServiceClientException
import com.projectronin.validation.clinical.data.client.work.exception.ServiceClientExceptionHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus

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

    private fun generateErrorServiceResponse(errResponse: ErrorResponse): ServiceResponse {
        val errResponseString = JsonProvider.objectMapper.writeValueAsString(errResponse)
        return ServiceResponse(errResponse.status, errResponseString)
    }
}
