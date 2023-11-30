package com.projectronin.product.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.configuration.SpringDocConfiguration
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.ObjectMapperProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@AutoConfiguration
open class OpenApiConfiguration {
    @Bean
    @ConditionalOnExpression("#{\${ronin.product.swagger.seki:true} and \${ronin.product.swagger.generated:true}}")
    open fun openAPI(): OpenAPI = OpenAPI().components(
        Components().addSecuritySchemes(
            "bearerAuth",
            SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
        )
    )

    @Bean
    @Primary
    @ConditionalOnExpression("#{!\${ronin.product.swagger.generated:true}}")
    open fun springDocConfiguration(): SpringDocConfiguration = SpringDocConfiguration()

    @Bean
    @ConditionalOnExpression("#{!\${ronin.product.swagger.generated:true}}")
    open fun springDocConfigProperties(): SpringDocConfigProperties = SpringDocConfigProperties()

    @Bean
    @ConditionalOnExpression("#{!\${ronin.product.swagger.generated:true}}")
    open fun objectMapperProvider(springDocConfigProperties: SpringDocConfigProperties): ObjectMapperProvider =
        ObjectMapperProvider(springDocConfigProperties)
}
