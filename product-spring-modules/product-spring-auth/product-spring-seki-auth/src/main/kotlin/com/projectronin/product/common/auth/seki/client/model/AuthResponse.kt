package com.projectronin.product.common.auth.seki.client.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Wrapper for seki response with the user's data and session
 *
 * @property user The user data
 * @property userSession The user's session data
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AuthResponse(
    val user: User,
    val userSession: UserSession
)
