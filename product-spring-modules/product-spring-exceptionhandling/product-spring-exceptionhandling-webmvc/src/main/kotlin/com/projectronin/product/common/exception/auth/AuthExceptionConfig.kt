package com.projectronin.product.common.exception.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.authentication.AuthenticationFailureHandler

@Configuration
open class AuthExceptionConfig {

    @Bean
    open fun authenticationFailureHandler(objectMapper: ObjectMapper): AuthenticationFailureHandler {
        return CustomAuthenticationFailureHandler(objectMapper)
    }
}
