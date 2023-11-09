package com.projectronin.product.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.auth.AuthErrorResponseGenerator
import com.projectronin.product.common.exception.response.api.ErrorResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus

@Configuration
open class AuthResponseGeneratorConfig {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    open fun authErrorResponseGenerator(objectMapper: ObjectMapper): AuthErrorResponseGenerator {
        return AuthErrorResponseGenerator { exception ->
            objectMapper.writeValueAsBytes(
                ErrorResponse.logAndCreateErrorResponse(
                    logger = logger,
                    httpStatus = HttpStatus.UNAUTHORIZED,
                    exception = exception,
                    message = "Authentication Error",
                    detail = exception.message
                )
            )
        }
    }
}
