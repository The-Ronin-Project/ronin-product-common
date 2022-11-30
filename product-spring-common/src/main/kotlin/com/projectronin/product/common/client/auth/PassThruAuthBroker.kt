package com.projectronin.product.common.client.auth

import org.springframework.http.HttpHeaders

class PassThruAuthBroker(val authToken: String) : AuthBroker {
    override fun generateAuthHeaders(): Map<String, String> {
        return if (authToken.isNotEmpty()) {
            mapOf(HttpHeaders.AUTHORIZATION to "Bearer $authToken")
        } else {
            emptyMap()
        }
    }
}
