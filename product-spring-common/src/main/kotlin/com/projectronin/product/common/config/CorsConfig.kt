package com.projectronin.product.common.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

private const val CORS_PROPERTIES_PREFIX = "ronin.product.cors"

@Configuration
@ConditionalOnProperty(name = [CORS_PROPERTIES_PREFIX], matchIfMissing = true)
@EnableConfigurationProperties(CorsProperties::class)
open class CorsConfig(private val config: CorsProperties) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping(config.path).apply {
            allowedOriginPatterns(*config.origins.toTypedArray())
            allowedHeaders(*config.headers.toTypedArray())
            allowCredentials(config.allowCredentials)
        }
    }
}

@ConstructorBinding
@ConfigurationProperties(CORS_PROPERTIES_PREFIX)
data class CorsProperties(
    @DefaultValue("/**")
    val path: String,
    @DefaultValue
    val origins: List<String>,
    @DefaultValue
    val headers: List<String>,
    @DefaultValue("false")
    val allowCredentials: Boolean
)
