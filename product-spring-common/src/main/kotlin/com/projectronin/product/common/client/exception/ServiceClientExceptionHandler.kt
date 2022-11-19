package com.projectronin.product.common.client.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.product.common.client.ServiceResponse
import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.exception.response.api.getExceptionName
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

open class ServiceClientExceptionHandler(protected val objectMapper: ObjectMapper = JsonProvider.objectMapper) {

    @Throws(ServiceClientException::class)
    open fun handleError(serviceResponse: ServiceResponse): Nothing {

        // for non-http exceptions (like 'Connection Refused' the resulting ServiceClientException
        //   will contain the 'cause' exception within.  Since we don't have an exception that wa
        //   actually thrown when got back a response with a 4xx/5xx error, we will create a new
        //   exception for that role so that all ServiceClientException will be consistent have have
        //   a nested casue exception.
        val causeException = createCauseException(serviceResponse)

        val errResponse = createErrorResponse(serviceResponse, causeException)

        throw ServiceClientException("Service Client Exception: ${causeException.message}", causeException, errResponse)
    }

    protected open fun createErrorResponse(serviceResponse: ServiceResponse, causeException: Exception? = null): ErrorResponse {
        try {
            // attempt to directly deserialize error response string into ErrorResponse
            //   to see if came from kotlin service that already uses this format
            return objectMapper.readValue(serviceResponse.body)
        } catch (e: Exception) {
            // if the error response string was not in the ErrorResponse form, then just create our own.
            return ErrorResponse(
                httpStatus = serviceResponse.httpStatus,
                exception = causeException?.getExceptionName() ?: "",
                message = serviceResponse.body,
                detail = serviceResponse.body
            )
        }
    }

    @Throws(ServiceClientException::class)
    open fun handleException(e: Exception): Nothing {
        if (e is ServiceClientException) {
            throw e
        }
        throw ServiceClientException("Service Client Exception: ${e.message}", e)
    }

    /**
     * Create a nested exception that represents the actual cause
     */
    protected open fun createCauseException(serviceResponse: ServiceResponse): Exception {
        // NOTE: DefaultResponseErrorHandler().handleError(...)
        //   will always throw an exception 100% of the time,
        //     thus the reason for the weird syntax
        var causalException = Exception("An Exception Has Occurred")
        try {
            DefaultResponseErrorHandler().handleError(SpringHttpRersposneAdapter(serviceResponse))
        } catch (e: Exception) {
            causalException = e
        }
        return causalException
    }

    /**
     * Special Adapter class to make the ServiceResponse to a ClientHttpResponse
     *   to be used for the DefaultResponseErrorHandler
     */
    private class SpringHttpRersposneAdapter(private val serviceResponse: ServiceResponse) : ClientHttpResponse {
        @Throws(IOException::class)
        override fun getStatusCode(): HttpStatus {
            return serviceResponse.httpStatus
        }

        @Throws(IOException::class)
        override fun getRawStatusCode(): Int {
            return serviceResponse.httpCode
        }

        @Throws(IOException::class)
        override fun getStatusText(): String {
            return serviceResponse.httpStatus.toString()
        }

        override fun close() {
            // No-Op - any necessary close on the response should have already been handled.
        }

        @Throws(IOException::class)
        override fun getBody(): InputStream {
            return ByteArrayInputStream(serviceResponse.body.toByteArray(StandardCharsets.UTF_8))
        }

        override fun getHeaders(): HttpHeaders {
            return serviceResponse.httpHeaders
        }
    }
}
