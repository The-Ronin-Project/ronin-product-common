package com.projectronin.product.common.exception.response

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import org.springframework.stereotype.Component

@Component
internal class AuthErrorResponseGenerator : AbstractErrorStatusResponseGenerator(HttpStatus.UNAUTHORIZED) {

    override fun getErrorMessageInfo(exception: Throwable, existingHttpStatus: HttpStatus?): ErrorMessageInfo? = when (exception) {
        is BadCredentialsException, is PreAuthenticatedCredentialsNotFoundException ->  ErrorMessageInfo("Authorization Error", exception.message)
        else -> null
    }

    override fun getOrder(): Int = 0
}
