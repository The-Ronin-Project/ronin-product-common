package com.projectronin.product.common.exception.auth

import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.response.api.AbstractErrorHandlingEntityBuilder
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.exception.response.api.getExceptionName
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

/**
 * Handles authentication failures and generates our standard response body for them.
 */
class CustomAuthenticationFailureHandler : AuthenticationFailureHandler, AbstractErrorHandlingEntityBuilder<AuthenticationException>() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val responseEntity = generateResponseEntity(exception)

        // set response to correct status
        response.status = responseEntity.statusCode.value()

        // set any additional headers from responseEntity to our response
        for (headerEntry in responseEntity.headers.toSingleValueMap()) {
            response.setHeader(headerEntry.key, headerEntry.value)
        }

        // marshal responseBody to response object.
        //    MUST do this LAST  (or else the response status won't get set correctly)
        JsonProvider.objectMapper.writeValue(response.outputStream, responseEntity.body)
    }

    override fun buildErrorResponse(exception: AuthenticationException, existingHttpStatus: HttpStatus?): ErrorResponse {
        return ErrorResponse(
            httpStatus = HttpStatus.UNAUTHORIZED,
            exception = exception.getExceptionName(),
            message = "Authentication Error",
            detail = exception.message,
        )
    }
}
