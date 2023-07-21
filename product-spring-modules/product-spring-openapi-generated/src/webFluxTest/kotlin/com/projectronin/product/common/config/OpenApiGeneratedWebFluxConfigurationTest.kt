package com.projectronin.product.common.config

import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [OpenApiConfiguration::class])
class OpenApiGeneratedWebFluxConfigurationTest {

    @Autowired
    private lateinit var openApi: OpenAPI

    @Test
    fun `assert exists`() {
        Assertions.assertEquals(1, openApi.components.securitySchemes.size)
        Assertions.assertEquals("seki", openApi.components.securitySchemes.keys.first())
    }
}
