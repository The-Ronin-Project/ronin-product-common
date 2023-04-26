package com.projectronin.product.common.config

import com.projectronin.product.common.exception.response.api.AbstractSimpleErrorHandlingEntityBuilder
import com.projectronin.product.common.exception.response.api.ErrorMessageInfo
import com.projectronin.product.common.exception.response.api.ErrorResponse
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody

@ControllerAdvice
@Order(0)
class AccessDeniedExceptionHandler : AbstractSimpleErrorHandlingEntityBuilder<AccessDeniedException>(HttpStatus.FORBIDDEN) {

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseBody
    fun handleMethodTypeArgumentTypeMismatchException(
        exception: AccessDeniedException
    ): ResponseEntity<ErrorResponse> {
        //  this is a catch-all for any exception types not already handled.
        return generateResponseEntity(exception)
    }

    override fun getErrorMessageInfo(
        exception: AccessDeniedException,
        existingHttpStatus: HttpStatus?
    ): ErrorMessageInfo {
        return ErrorMessageInfo("Forbidden", exception.message)
    }
}
