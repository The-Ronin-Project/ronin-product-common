package com.projectronin.product.common.auth.seki.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserSession(
    val expiresAt: String? = null,
    val metadata: Map<String, Any>? = null,
    val tokenString: String? = null
)
