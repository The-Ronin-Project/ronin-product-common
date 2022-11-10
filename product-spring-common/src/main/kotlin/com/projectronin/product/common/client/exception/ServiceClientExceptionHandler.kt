package com.projectronin.validation.clinical.data.client.work.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.client.ServiceResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

open class ServiceClientExceptionHandler(protected val objectMapper: ObjectMapper) {

    @Throws(ServiceClientException::class)
    open fun handleError(serviceResponse: ServiceResponse): Nothing {

        // try {
        //     // unable to convert to object (at the moment) b/c of the HttpStatus val defn
        //     val errorMap = objectMapper.readValue(serviceResponse.body, object : TypeReference<Map<String, Any?>>() {})
        //     // TODO - implement
        // }
        // catch (e: Exception) {
        //     // TODO - what if this happens?
        // }

        // create a causal exception to nest inside the exception to be thrown
        //   (note: this may not be strictly necessary, but is ok for now)
        val causalException = createCauseException(serviceResponse)

        throw ServiceClientException("Service Client Exception: ${causalException.message}", causalException, serviceResponse)
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
     *   TODO: this may be overkill and not really necessary.  Subject to removal
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
