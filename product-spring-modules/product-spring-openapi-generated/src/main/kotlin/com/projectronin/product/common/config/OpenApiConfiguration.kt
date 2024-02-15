package com.projectronin.product.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnExpression("#{\${ronin.product.swagger.seki:true} and \${ronin.product.swagger.generated:true}}")
open class OpenApiConfiguration {
    @Bean
    open fun openAPI(): OpenAPI = OpenAPI().components(
        Components().addSecuritySchemes(
            "bearerAuth",
            SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
        )
    )
}
