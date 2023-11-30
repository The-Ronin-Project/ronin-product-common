package com.projectronin.product.common.config

import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.StringContains.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "PT30S")
@EnableAutoConfiguration
class WfOpenApiBasicTest {

    @Autowired
    private lateinit var openApi: OpenAPI

    @Autowired
    private lateinit var webTestClient: WebTestClient

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
        webTestClient.get()
            .uri("/v3/api-docs")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("\$.paths['/api/test/sample-object']").exists()
            .jsonPath("\$.components.securitySchemes.bearerAuth").exists()
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
            .value(containsString("<title>Swagger UI</title>"))
    }
}
