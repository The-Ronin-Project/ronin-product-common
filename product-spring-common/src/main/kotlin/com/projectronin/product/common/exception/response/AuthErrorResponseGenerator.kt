package com.projectronin.product.common.exception.response

import org.springframework.http.HttpStatus

internal class AuthErrorResponseGenerator : AbstractErrorStatusResponseGenerator(HttpStatus.UNAUTHORIZED) {

    override fun getErrorMessageInfo(exception: Throwable): ErrorMessageInfo {
        return ErrorMessageInfo("Authorization Error", exception.message)
    }
}
