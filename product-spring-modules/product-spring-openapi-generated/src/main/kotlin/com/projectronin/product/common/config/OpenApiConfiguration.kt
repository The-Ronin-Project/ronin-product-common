package com.projectronin.product.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OpenApiConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "ronin.product.swagger", name = ["seki"], matchIfMissing = true)
    open fun openAPI(): OpenAPI = OpenAPI().components(
        Components().addSecuritySchemes(
            "seki",
            SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
        )
    )
}
