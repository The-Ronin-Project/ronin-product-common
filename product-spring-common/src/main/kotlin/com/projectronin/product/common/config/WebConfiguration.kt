package com.projectronin.product.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class WebConfiguration {
    @Bean
    open fun openAPI(): OpenAPI = OpenAPI().components(
        Components().addSecuritySchemes(
            "seki",
            SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
        )
    )
}
