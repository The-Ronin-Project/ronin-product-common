package com.projectronin.product.common.exception

import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.response.ErrorResponse
import com.projectronin.product.common.exception.response.ErrorStatusResponseGenerator
import com.projectronin.product.common.exception.response.InternalErrorResponseGenerator
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Converts all exceptions into a common error response output.
 *
 * It does this by taking the exception and any associated HttpStatus and iterating through
 * a list of `ErrorStatusResponseGenerator`s, using the first one that returns a non-null ErrorResponse.  Spring should then
 * serialize this response and return the status that the handler determined.
 *
 * The `ErrorStatusResponseGenerator` objects are Spring components, ordered by the spring `Ordered` interface.
 * The highest-priority (lowest order) generator that returns a response wins.  So to inject different error
 * handling into the list, you need to create a new (prioritized) ErrorStatusResponseGenerator implementation.
 * If the exception you're trying to handle doesn't overlap with other handled exceptions, an order of `0` is fine.
 * If it does, then you probably want to set its order lower than 0 to make sure it's included first.
 *
 * @see ErrorStatusResponseGenerator
 */
@ControllerAdvice
class CustomErrorHandler(private val responseGenerators: List<ErrorStatusResponseGenerator>) :
    ResponseEntityExceptionHandler(), AuthenticationFailureHandler {

    /**
     * {@inheritDoc}
     * Default handling for common client exceptions
     */
    public override fun handleExceptionInternal(
        ex: java.lang.Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> {
        // the parent ResponseEntityExceptionHandler has already figured out
        //    the correct statusCode for many (_but not all_) exceptions
        //    (this is useful for errors like 405, 406, 415, etc)
        @Suppress("UNCHECKED_CAST")
        return generateResponseEntity(ex, status) as ResponseEntity<Any>
    }

    /**
     * All Exceptions are handled here IFF not handled by the 'handleExceptionInternal' method above.
     */
    @ExceptionHandler
    @ResponseBody
    fun defaultHandleException(exception: Exception, request: WebRequest?): ResponseEntity<ErrorResponse> {
        //  this is a catch-all for any exception types not already handled.
        return generateResponseEntity(exception)
    }

    /**
     * {@inheritDoc}
     * Special exception handling method for Pre-Authorization exceptions
     */
    @ResponseBody
    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val responseEntity = generateResponseEntity(exception)

        // set response to correct status
        response.status = responseEntity.statusCodeValue

        // set any additional headers from responseEntity to our response
        for (headerEntry in responseEntity.headers.toSingleValueMap()) {
            response.setHeader(headerEntry.key, headerEntry.value)
        }

        // marshal responseBody to response object.
        //    MUST do this LAST  (or else the response status won't get set correctly)
        JsonProvider.objectMapper.writeValue(response.outputStream, responseEntity.body)
    }

    /**
     * Generate an ResponseEntity error response based on the exception
     */
    private fun generateResponseEntity(
        exception: Exception,
        existingStatus: HttpStatus? = null
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = generateErrorResponse(exception, existingStatus)

        return ResponseEntity
            .status(errorResponse.httpStatus)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    /**
     * Create an ErrorResponse based on exception
     * @param exception the exception used for the error response
     * @param existingStatus (optional) hint status to use for errorResponse if statusCode not determined by exception
     * @return the errorResponse object
     */
    private fun generateErrorResponse(exception: Exception, existingStatus: HttpStatus? = null): ErrorResponse {
        val nestedException = getNestedException(exception)
        val errorResponse = getErrorResponseFromException(nestedException, existingStatus)

        val loggableErrorMessage =
            "Request error: ${errorResponse.message}, ${errorResponse.detail}, ${errorResponse.exception}"
        if (errorResponse.httpStatus.is5xxServerError) {
            logger.error(loggableErrorMessage, exception)
        } else {
            // don't need to pass in exception for stacktrace for these types of client errors
            logger.warn(loggableErrorMessage)
        }
        return errorResponse
    }

    /**
     * Find 'true' exception, which might be nested.
     */
    private fun getNestedException(exception: Throwable): Throwable {
        var nestedException: Throwable = exception
        while (nestedException is ServletException || nestedException is HttpMessageNotReadableException) {
            nestedException = nestedException.cause ?: break
        }
        return nestedException
    }

    /**
     * Converts an exception into an error response
     *
     * @param exception The thrown exception
     * @param existingHttpStatus The status, if any, that was returned with the exception
     */
    private fun getErrorResponseFromException(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorResponse {
        val nestedException = getNestedException(exception)

        return responseGenerators
            .firstNotNullOfOrNull { responseGenerator ->
                responseGenerator.buildErrorResponse(
                    nestedException,
                    existingHttpStatus
                )
            } ?: InternalErrorResponseGenerator.buildErrorResponse(nestedException, existingHttpStatus)
    }
}
