package com.projectronin.product.common.exception.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.exception.response.api.ErrorHandlingResponseEntityConstructor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.authentication.AuthenticationFailureHandler

@Configuration
open class AuthExceptionConfig {

    @Bean
    open fun authenticationFailureHandler(objectMapper: ObjectMapper, responseEntityConstructor: ErrorHandlingResponseEntityConstructor): AuthenticationFailureHandler {
        return CustomAuthenticationFailureHandler(objectMapper, responseEntityConstructor)
    }
}
