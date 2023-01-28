package com.projectronin.product.common.exception.auth

import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.response.api.AbstractErrorHandlingEntityBuilder
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.exception.response.api.getExceptionName
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.web.bind.annotation.ResponseStatus
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ResponseStatus(HttpStatus.FORBIDDEN)
/**
 * Handles authentication failures and generates our standard response body for them.
 */
class CustomAuthenticationFailureHandler : AuthenticationFailureHandler, AbstractErrorHandlingEntityBuilder<AuthenticationException>() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        logger.error("11) building response entity")
        val responseEntity = generateResponseEntity(exception)
        logger.error("12) built response entity")

        // set response to correct status
        response.status = responseEntity.statusCodeValue

        // set any additional headers from responseEntity to our response
        logger.error("13) setting headers")
        for (headerEntry in responseEntity.headers.toSingleValueMap()) {
            response.setHeader(headerEntry.key, headerEntry.value)
        }

        // marshal responseBody to response object.
        //    MUST do this LAST  (or else the response status won't get set correctly)
        logger.error("14) mapping object")
        JsonProvider.objectMapper.writeValue(response.outputStream, responseEntity.body)
        logger.error("15) mapped object")
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
