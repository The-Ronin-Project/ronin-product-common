package com.projectronin.product.common.config

import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration
class MvcOpenApiBasicTest {

    @Autowired
    private lateinit var openApi: OpenAPI

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should contain bearer auth security`() {
        assertThat(openApi.components.securitySchemes).hasSize(1)
        with(openApi.components.securitySchemes.entries.first()) {
            assertThat(key).isEqualTo("bearerAuth")
            assertThat(value.scheme).isEqualTo("bearer")
            assertThat(value.bearerFormat).isEqualTo("JWT")
        }
    }

    @Test
    fun `should serve actual generated openapi spec`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v3/api-docs")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("\$.paths['/api/test/sample-object']").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("\$.components.securitySchemes.bearerAuth").exists())
    }

    @Test
    fun `should serve GUI`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/swagger-ui/index.html")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string(StringContains.containsString("<title>Swagger UI</title>")))
    }
}
