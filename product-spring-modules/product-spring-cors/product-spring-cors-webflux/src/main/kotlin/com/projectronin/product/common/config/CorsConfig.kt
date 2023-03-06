package com.projectronin.product.common.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@ConditionalOnProperty(name = [CORS_PROPERTIES_PREFIX], matchIfMissing = true)
@EnableConfigurationProperties(CorsProperties::class)
open class CorsConfig(private val config: CorsProperties) : WebFluxConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping(config.path).apply {
            allowedOriginPatterns(*config.origins.toTypedArray())
            allowedHeaders(*config.headers.toTypedArray())
            allowCredentials(config.allowCredentials)
        }
    }
}
