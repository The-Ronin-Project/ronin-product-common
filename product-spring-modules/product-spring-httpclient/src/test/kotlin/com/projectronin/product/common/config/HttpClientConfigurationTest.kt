package com.projectronin.product.common.config

import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [HttpClientConfiguration::class])
class HttpClientConfigurationTest {

    @Autowired
    private lateinit var httpClient: OkHttpClient

    // confirm client config gives expected seki client
    @Test
    fun `get an http client`() {
        assertThat(httpClient).isNotNull
    }
}
