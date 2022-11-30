package com.projectronin.product.common.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HttpClientConfigurationTest {

    // confirm client config gives expected seki client
    @Test
    fun `get seki client`() {
        val config = HttpClientConfiguration()
        val sekiHost = "https://myseki"
        val sekiClient = config.getSekiClient(sekiHost, config.getHttpClient())
        // confirm the seki client has the host we gave
        assertEquals(sekiHost, sekiClient.baseUrl, "mismatch expectecd seki host url")
    }
}
