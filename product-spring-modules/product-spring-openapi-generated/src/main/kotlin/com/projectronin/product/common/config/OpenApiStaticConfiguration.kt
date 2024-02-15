package com.projectronin.product.common.config

import org.springdoc.core.configuration.SpringDocConfiguration
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.ObjectMapperProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary

@AutoConfiguration
@ConditionalOnExpression("#{!\${ronin.product.swagger.generated:true}}")
open class OpenApiStaticConfiguration {
    // @Bean
    // @Primary
    // open fun springDocConfigProperties(): SpringDocConfigProperties {
    //     return SpringDocConfigProperties().also { it.apiDocs.path = "/not-accessible" }
    // }
    //
    // @Bean
    // @Primary
    // open fun objectMapperProvider(springDocConfigProperties: SpringDocConfigProperties): ObjectMapperProvider =
    //     ObjectMapperProvider(springDocConfigProperties)
}
