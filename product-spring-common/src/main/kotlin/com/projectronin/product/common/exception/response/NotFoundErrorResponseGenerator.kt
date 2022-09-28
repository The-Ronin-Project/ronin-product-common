package com.projectronin.product.common.exception.response

import com.projectronin.product.common.exception.NotFoundException
import org.springframework.http.HttpStatus

internal class NotFoundErrorResponseGenerator : AbstractErrorStatusResponseGenerator(HttpStatus.NOT_FOUND) {

    override fun getErrorMessageInfo(exception: Throwable): ErrorMessageInfo {

        when (exception) {
            is NotFoundException -> {
                return ErrorMessageInfo("Not Found", "Item was not found: ${exception.id}")
            }
            // Any other exception is a generic invalid request
            else -> {
                return ErrorMessageInfo("Unhandled NotFound Request", exception.message)
            }
        }
    }
}
