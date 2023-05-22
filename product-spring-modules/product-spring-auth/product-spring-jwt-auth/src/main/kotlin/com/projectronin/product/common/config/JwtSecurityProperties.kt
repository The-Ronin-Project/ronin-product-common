package com.projectronin.product.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

const val JWT_SECURITY_PROPERTIES_PREFIX = "ronin.auth"
const val SEKI_ISSUER_NAME = "Seki"

@ConfigurationProperties(JWT_SECURITY_PROPERTIES_PREFIX)
data class JwtSecurityProperties @ConstructorBinding constructor(
    @DefaultValue("Seki")
    val issuers: List<String>,
    val sekiSharedSecret: String? = null,
    @DefaultValue("/api/**")
    val securedPathPatterns: List<String> = listOf("/api/**"),
    @DefaultValue("/actuator/**", "/swagger-ui/**", "/v3/api-docs/swagger-config", "/v*/*.json", "/error")
    val permittedPathPatterns: List<String> = listOf("/actuator/**", "/swagger-ui/**", "/v3/api-docs/swagger-config", "/v*/*.json", "/error"),
    @DefaultValue("/**")
    val matchedPathPatterns: List<String> = listOf("/**"),
    @DefaultValue("false")
    val detailedErrors: Boolean = false,
    val validAudiences: List<String>? = null,
    @DefaultValue("STATELESS")
    val sessionCreationPolicy: String = "STATELESS",
    @DefaultValue("true")
    val disableCsrf: Boolean = true
)
