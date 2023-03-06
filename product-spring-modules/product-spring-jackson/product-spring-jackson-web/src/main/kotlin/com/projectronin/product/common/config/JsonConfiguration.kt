package com.projectronin.product.common.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@ConditionalOnProperty(prefix = "ronin.product", name = ["json"], matchIfMissing = true)
open class JsonConfiguration {
    @Bean
    @Primary
    open fun objectMapper() = JsonProvider.objectMapper
}
