package com.projectronin.product.common.exception.response

import org.springframework.http.HttpStatus

/**
 * A default error generator when you have an existing httpStatus code,
 *   and there's nothing special to do with it.
 */
internal class GenericStatusCodeResponseGenerator(status: HttpStatus) : AbstractErrorStatusResponseGenerator(status) {

    override fun getErrorMessageInfo(exception: Throwable): ErrorMessageInfo {
        return ErrorMessageInfo(this.httpStatus.reasonPhrase, exception.message)
    }
}
