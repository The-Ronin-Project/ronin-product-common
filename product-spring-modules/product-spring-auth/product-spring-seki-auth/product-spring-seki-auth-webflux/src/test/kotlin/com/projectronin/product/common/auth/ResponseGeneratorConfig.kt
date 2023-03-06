package com.projectronin.product.common.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ResponseGeneratorConfig {

    @Bean
    open fun authErrorResponseGenerator(): AuthErrorResponseGenerator {
        return AuthErrorResponseGenerator { _ -> """{"error": "unauthorized"}""".toByteArray() }
    }
}
