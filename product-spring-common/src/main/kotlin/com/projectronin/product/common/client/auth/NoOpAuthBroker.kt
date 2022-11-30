package com.projectronin.product.common.client.auth

object NoOpAuthBroker : AuthBroker {
    override fun generateAuthHeaders(): Map<String, String> {
        return emptyMap()
    }
}
