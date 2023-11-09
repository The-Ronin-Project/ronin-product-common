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
    val additionalSecuredPathPatterns: List<String>? = null,
    @DefaultValue("/actuator/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v*/openapi.json", "/error", "/webjars/swagger-ui/**")
    val permittedPathPatterns: List<String> = listOf("/actuator/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v*/openapi.json", "/error", "/webjars/swagger-ui/**"),
    val additionalPermittedPathPatterns: List<String>? = null,
    @DefaultValue("/**")
    val matchedPathPatterns: List<String> = listOf("/**"),
    val additionalMatchedPathPatterns: List<String>? = null,
    val validAudiences: List<String>? = null,
    @DefaultValue("STATELESS")
    val sessionCreationPolicy: String = "STATELESS",
    @DefaultValue("true")
    val disableCsrf: Boolean = true
) {
    fun combinedSecuredPathPatterns(): List<String> = securedPathPatterns + (additionalSecuredPathPatterns ?: emptyList())
    fun combinedPermittedPathPatterns(): List<String> = permittedPathPatterns + (additionalPermittedPathPatterns ?: emptyList())
    fun combinedMatchedPathPatterns(): List<String> = matchedPathPatterns + (additionalMatchedPathPatterns ?: emptyList())
}
