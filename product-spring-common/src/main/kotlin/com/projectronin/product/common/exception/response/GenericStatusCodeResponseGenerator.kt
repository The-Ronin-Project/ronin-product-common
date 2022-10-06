package com.projectronin.product.common.exception.response

import com.projectronin.product.common.exception.HttpStatusBearingException
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

/**
 * A default error generator when you have an existing httpStatus code,
 *   and there's nothing special to do with it.
 */
@Component
internal class GenericStatusCodeResponseGenerator : AbstractErrorStatusResponseGenerator(HttpStatus.INTERNAL_SERVER_ERROR) {

    override fun getErrorMessageInfo(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorMessageInfo? {
        return if (existingHttpStatus != null) {
            ErrorMessageInfo(existingHttpStatus.reasonPhrase, exception.message)
        } else {
            null
        }
    }

    override fun getHttpStatus(exception: Throwable, existingHttpStatus: HttpStatus?): HttpStatus {
        return existingHttpStatus ?: super.getHttpStatus(exception, null)
    }
    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE - 20
}
