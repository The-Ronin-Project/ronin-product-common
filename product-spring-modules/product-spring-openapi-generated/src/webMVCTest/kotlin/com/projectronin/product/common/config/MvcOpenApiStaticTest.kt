package com.projectronin.product.common.config

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.Test
import org.springdoc.core.properties.SwaggerUiConfigProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest(
    properties = [
        "ronin.product.swagger.generated=false",
        "springdoc.swagger-ui.url=/v3/api-docs/contract-test/test.json",
        "springdoc.api-docs.path=/not-accessible"
    ]
)
@AutoConfigureMockMvc
@EnableAutoConfiguration
class MvcOpenApiStaticTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var swaggerUiConfigProperties: SwaggerUiConfigProperties

    @Test
    fun `should serve actual static openapi spec`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(swaggerUiConfigProperties.url)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("\$.paths['/api/v1/some-test-path']").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("\$.components.securitySchemes.foo").exists())
    }

    @Test
    fun `should not serve generated openapi spec`() {
        val body = mockMvc.perform(
            MockMvcRequestBuilders.get("/v3/api-docs")
        )
            .andReturn().response.contentAsString
        assertThat(body).doesNotContain("Generated")
    }

    @Test
    fun `should serve GUI`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/swagger-ui/index.html")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string(StringContains.containsString("<title>Swagger UI</title>")))
    }

    @Test
    fun `should have right config`() {
        assertThat(swaggerUiConfigProperties.url).isEqualTo("/v3/api-docs/contract-test/test.json")
    }
}
