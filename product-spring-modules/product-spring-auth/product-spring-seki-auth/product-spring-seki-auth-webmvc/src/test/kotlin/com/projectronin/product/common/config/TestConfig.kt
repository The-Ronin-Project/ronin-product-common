package com.projectronin.product.common.config

import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.web.authentication.AuthenticationFailureHandler

@Configuration
open class TestConfig {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    open fun authenticationFailureHandler(): AuthenticationFailureHandler {
        return AuthenticationFailureHandler { _, response, exception ->
            logger.error("Auth error", exception)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, HttpStatus.UNAUTHORIZED.reasonPhrase)
        }
    }
}
