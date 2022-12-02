package com.projectronin.product.common.client.auth

interface AuthBroker {

    fun generateAuthHeaders(): Map<String, String>
}
