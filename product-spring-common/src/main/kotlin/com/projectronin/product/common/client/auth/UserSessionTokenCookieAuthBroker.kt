package com.projectronin.product.common.client.auth

import org.springframework.http.HttpHeaders
import java.time.Instant

class UserSessionTokenCookieAuthBroker(val authToken: String, val state: Long = Instant.now().epochSecond) : AuthBroker {

    private val authHeaderMap: Map<String, String>
    init {
        authHeaderMap = mapOf(
            HttpHeaders.COOKIE to "user_session_token_$state=$authToken",
            "X-STATE" to "$state"
        )
    }

    override fun generateAuthHeaders(): Map<String, String> {
        return authHeaderMap
    }
}
