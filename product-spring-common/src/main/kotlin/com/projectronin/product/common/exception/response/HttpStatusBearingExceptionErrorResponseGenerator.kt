package com.projectronin.product.common.exception.response

import com.projectronin.product.common.exception.HttpStatusBearingException
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
internal class HttpStatusBearingExceptionErrorResponseGenerator :
    AbstractErrorStatusResponseGenerator(HttpStatus.INTERNAL_SERVER_ERROR) {

    override fun getHttpStatus(exception: Throwable, existingHttpStatus: HttpStatus?): HttpStatus {
        return when (exception) {
            is HttpStatusBearingException -> {
                exception.httpStatus
            }
            // Any other exception is a generic ISE
            else -> {
                super.getHttpStatus(exception, null)
            }
        }
    }

    override fun getErrorMessageInfo(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorMessageInfo? =
        when (exception) {
            is HttpStatusBearingException -> {
                ErrorMessageInfo(exception.errorResponseMessage, exception.errorResponseDetail)
            }

            else -> null
        }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE - 30
}
