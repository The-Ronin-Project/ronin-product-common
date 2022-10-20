package com.projectronin.product.common.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(prefix = "ronin.product", name = ["json"], matchIfMissing = true)
open class JsonConfiguration {
    @Bean
    open fun objectMapper() = JsonProvider.objectMapper
}
