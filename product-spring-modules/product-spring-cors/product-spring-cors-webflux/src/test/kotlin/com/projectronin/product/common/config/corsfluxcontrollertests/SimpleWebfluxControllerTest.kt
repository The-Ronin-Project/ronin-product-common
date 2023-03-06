package com.projectronin.product.common.config.corsfluxcontrollertests

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(SimpleWebfluxController::class)
@Import(SharedWebfluxConfigurationReference::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleWebfluxControllerTest(
    @Autowired val webTestClient: WebTestClient
) {

    @Test
    fun testCorsSuccess() {
        webTestClient
            .get()
            .uri("https://foo.bar/api/test/sample-object")
            .header(HttpHeaders.ORIGIN, "https://www.example.com")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "origin", "x-requested-with")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://www.example.com")
    }

    @Test
    fun testCorsFailure() {
        webTestClient
            .get()
            .uri("https://foo.bar/api/test/sample-object")
            .header(HttpHeaders.ORIGIN, "https://projectronin.io")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "origin", "x-requested-with")
            .exchange()
            .expectStatus().isForbidden
    }
}
