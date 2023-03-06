package com.projectronin.product.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

const val CORS_PROPERTIES_PREFIX = "ronin.product.cors"

@ConfigurationProperties(CORS_PROPERTIES_PREFIX)
data class CorsProperties @ConstructorBinding constructor(
    @DefaultValue("/**")
    val path: String,
    @DefaultValue
    val origins: List<String>,
    @DefaultValue
    val headers: List<String>,
    @DefaultValue("false")
    val allowCredentials: Boolean
)
