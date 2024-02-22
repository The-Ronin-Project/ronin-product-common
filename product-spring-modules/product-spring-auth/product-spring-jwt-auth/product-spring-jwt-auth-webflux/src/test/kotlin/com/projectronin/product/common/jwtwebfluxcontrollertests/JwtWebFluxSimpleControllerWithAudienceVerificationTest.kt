@file:Suppress("ktlint:no-wildcard-imports")

package com.projectronin.product.common.jwtwebfluxcontrollertests

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.resetToDefault
import com.projectronin.product.common.auth.RoninJwtAuthenticationToken
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.testconfigs.AudiencePropertiesConfig
import com.projectronin.test.jwt.generateRandomRsa
import com.projectronin.test.jwt.withAuthWiremockServer
import io.mockk.clearAllMocks
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(JwtWebFluxSimpleController::class)
@Import(JwtWebFluxSharedConfigurationReference::class, AudiencePropertiesConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtWebFluxSimpleControllerWithAudienceVerificationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val authHolderBean: AuthHolderBean,
    @Autowired val config: AudiencePropertiesConfig
) {

    val wireMockServer: WireMockServer
        get() = config.wireMockServer

    @BeforeEach
    fun setup() {
        clearAllMocks()
        resetToDefault()
        authHolderBean.reset()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        resetToDefault()
        authHolderBean.reset()
    }

    @Test
    fun `should fail with no audience`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val token = jwtAuthToken()

            webTestClient
                .get()
                .uri("/api/test/sample-object")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()
                .expectStatus().isUnauthorized
                .expectBody()
                .consumeWith(::verifyUnauthorizedBody)
        }
    }

    fun `should fail with invalid audience`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val token = jwtAuthToken {
                withAudience("https://example.org")
            }

            webTestClient
                .get()
                .uri("/api/test/sample-object")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()
                .expectStatus().isUnauthorized
                .expectBody()
                .consumeWith(::verifyUnauthorizedBody)
        }
    }

    @Test
    fun `should be successful with valid token`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val token = jwtAuthToken {
                withAudience(wireMockServer.baseUrl())
            }

            webTestClient
                .get()
                .uri("/api/test/sample-object")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith { _ ->
                    assertThat(authHolderBean.latestRoninAuth).isNotNull
                    val authValue = authHolderBean.latestRoninAuth!!
                    assertThat(authValue).isInstanceOfAny(RoninJwtAuthenticationToken::class.java)
                }
        }
    }

    private fun verifyUnauthorizedBody(result: EntityExchangeResult<ByteArray>) {
        val body: ErrorResponse = objectMapper.readValue(result.responseBody!!)

        assertThat(body.httpStatus).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(body.timestamp).isNotNull
        assertThat(body.status).isEqualTo(401)
        assertThat(body.message).isEqualTo("Authentication Error")
    }
}
