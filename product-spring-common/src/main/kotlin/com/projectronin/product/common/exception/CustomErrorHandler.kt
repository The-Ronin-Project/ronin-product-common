package com.projectronin.product.common.exception

import com.fasterxml.jackson.core.JsonProcessingException
import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.response.AuthErrorResponseGenerator
import com.projectronin.product.common.exception.response.BadRequestErrorResponseGenerator
import com.projectronin.product.common.exception.response.ErrorResponse
import com.projectronin.product.common.exception.response.GenericStatusCodeResponseGenerator
import com.projectronin.product.common.exception.response.InternalErrorResponseGenerator
import com.projectronin.product.common.exception.response.NotFoundErrorResponseGenerator
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.ValidationException

/**
 * Converts all exceptions into a common error response output
 */
@ControllerAdvice
class CustomErrorHandler : ResponseEntityExceptionHandler(), AuthenticationFailureHandler {

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
    private fun generateResponseEntity(exception: Exception, existingStatus: HttpStatus? = null): ResponseEntity<ErrorResponse> {
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

        val loggableErrorMessage = "Request error: ${errorResponse.message}, ${errorResponse.detail}, ${errorResponse.exception}"
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

        return when (nestedException) {
            is BadCredentialsException, is PreAuthenticatedCredentialsNotFoundException -> AuthErrorResponseGenerator()
            is ValidationException, is JsonProcessingException, is BindException, is HttpMessageNotReadableException, is MethodArgumentTypeMismatchException -> {
                BadRequestErrorResponseGenerator()
            }
            is NotFoundException -> {
                NotFoundErrorResponseGenerator()
            }
            else -> if (existingHttpStatus != null) {
                GenericStatusCodeResponseGenerator(existingHttpStatus)
            } else {
                InternalErrorResponseGenerator()
            }
        }.buildErrorResponse(nestedException)
    }
}
