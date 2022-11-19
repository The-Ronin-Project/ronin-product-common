package com.projectronin.product.common.client.auth

object NoOpAuthBroker : AuthBroker {
    override val authToken: String
        get() = ""
}
