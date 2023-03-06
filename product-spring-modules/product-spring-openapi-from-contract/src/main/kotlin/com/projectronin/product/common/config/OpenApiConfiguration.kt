package com.projectronin.product.common.config

import org.springdoc.core.configuration.SpringDocConfiguration
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.ObjectMapperProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OpenApiConfiguration {
    @Bean
    open fun springDocConfiguration(): SpringDocConfiguration = SpringDocConfiguration()

    @Bean
    open fun springDocConfigProperties(): SpringDocConfigProperties = SpringDocConfigProperties()

    @Bean
    open fun objectMapperProvider(springDocConfigProperties: SpringDocConfigProperties): ObjectMapperProvider =
        ObjectMapperProvider(springDocConfigProperties)
}
