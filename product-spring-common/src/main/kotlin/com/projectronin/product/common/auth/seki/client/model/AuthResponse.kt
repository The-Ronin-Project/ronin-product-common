package com.projectronin.product.common.auth.seki.client.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AuthResponse(
    val user: User,
    val userSession: UserSession
)
