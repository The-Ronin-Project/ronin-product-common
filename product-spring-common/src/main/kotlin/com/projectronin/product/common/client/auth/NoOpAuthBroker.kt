package com.projectronin.validation.clinical.data.client.work.auth

object NoOpAuthBroker : AuthBroker {
    override val authToken: String
        get() = ""
}
