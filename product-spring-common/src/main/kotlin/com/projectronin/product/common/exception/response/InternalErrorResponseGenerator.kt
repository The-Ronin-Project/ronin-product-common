package com.projectronin.product.common.exception.response

import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus

internal class InternalErrorResponseGenerator : AbstractErrorStatusResponseGenerator(HttpStatus.INTERNAL_SERVER_ERROR) {

    override fun getErrorMessageInfo(exception: Throwable): ErrorMessageInfo {

        if (exception is DataAccessException) {
            return ErrorMessageInfo("Internal Database Error", exception.message)
        }

        return ErrorMessageInfo("Internal Error", exception.message)
    }
}
