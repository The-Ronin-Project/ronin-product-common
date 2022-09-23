package com.projectronin.product.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class JsonConfiguration {
    @Bean
    open fun objectMapper() = JsonProvider.objectMapper
}
