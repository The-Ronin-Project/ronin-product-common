package com.projectronin.product.common.client.examples

import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

private const val SEKI_URL = "https://seki.dev.projectronin.io/"
private const val AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2NTMzMjgzMjAsImlzcyI6IlNla2kiLCJqdGkiOiIycm9zcW05M2VlbmFwYmlrZm8wMXFrODEiLCJzdWIiOiIxNTFhMjUwOS1lNjllLTQwNDMtYmJhOC1kYmY5ODhkZGE1NTUiLCJ0ZW5hbnRpZCI6ImFwcG9zbmQifQ.gmX_Ad6sgTTW0iogI4kwuhYYbnpn5HGIE5RZxi56Ojs"

class SekiDemoTest {
    /**
     * Example how to make 'real calls' to Seki.
     */
    @Disabled
    @Test
    fun executeAuditDemo() {
        val sekiClient = SekiClient(SEKI_URL)

        val authResponse: AuthResponse = sekiClient.validate(AUTH_TOKEN)
        println(authResponse)

        val healthResult = sekiClient.health()
        println(healthResult)
    }
}
