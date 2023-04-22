package com.projectronin.product.common.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@AutoConfiguration
@ConditionalOnProperty(prefix = "ronin.product", name = ["json"], matchIfMissing = true)
open class JsonConfiguration {
    @Bean
    @Primary
    open fun objectMapper() = JsonProvider.objectMapper
}
