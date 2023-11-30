package com.projectronin.product.common.config

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.Test
import org.springdoc.core.properties.SwaggerUiConfigProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult

@SpringBootTest(properties = [
    "ronin.product.swagger.generated=false",
    "springdoc.swagger-ui.url=/v3/api-docs/contract-test/test.json"
])
@AutoConfigureWebTestClient(timeout = "PT30S")
@EnableAutoConfiguration
class WfOpenApiStaticTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var swaggerUiConfigProperties: SwaggerUiConfigProperties

    @Test
    fun `should serve actual generated openapi spec`() {
        webTestClient.get()
            .uri(swaggerUiConfigProperties.url)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("\$.paths['/api/v1/some-test-path']").exists()
            .jsonPath("\$.components.securitySchemes.foo").exists()
    }

    @Test
    fun `should serve GUI`() {
        val location = webTestClient.get()
            .uri("/swagger-ui.html")
            .exchange()
            .expectStatus().is3xxRedirection
            .expectHeader().exists("Location")
            .returnResult<Any>().responseHeaders["Location"]!!.first()

        webTestClient.get()
            .uri(location)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .value(StringContains.containsString("<title>Swagger UI</title>"))
    }

    @Test
    fun `should have right config`() {
        assertThat(swaggerUiConfigProperties.url).isEqualTo("/v3/api-docs/contract-test/test.json")
    }
}
