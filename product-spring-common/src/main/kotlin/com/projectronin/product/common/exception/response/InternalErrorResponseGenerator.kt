package com.projectronin.product.common.exception.response

import org.springframework.http.HttpStatus

internal class InternalErrorResponseGenerator : AbstractErrorStatusResponseGenerator(HttpStatus.INTERNAL_SERVER_ERROR) {

    override fun getErrorMessageInfo(exception: Throwable): ErrorMessageInfo {
        return ErrorMessageInfo("Internal Error", exception.message)
    }
}
