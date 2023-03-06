package com.projectronin.product.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.auth.AuthErrorResponseGenerator
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.exception.response.api.getExceptionName
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus

@Configuration
open class AuthResponseGeneratorConfig {
    @Bean
    open fun authErrorResponseGenerator(objectMapper: ObjectMapper): AuthErrorResponseGenerator {
        return AuthErrorResponseGenerator { exception ->
            objectMapper.writeValueAsBytes(
                ErrorResponse(
                    httpStatus = HttpStatus.UNAUTHORIZED,
                    exception = exception.getExceptionName(),
                    message = "Authentication Error",
                    detail = exception.message
                )
            )
        }
    }
}
