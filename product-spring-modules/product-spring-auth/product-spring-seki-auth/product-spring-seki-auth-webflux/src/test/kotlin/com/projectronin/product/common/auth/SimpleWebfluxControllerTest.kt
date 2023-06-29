package com.projectronin.product.common.auth

import com.ninjasquad.springmockk.MockkBean
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.exception.SekiClientException
import com.projectronin.product.common.auth.seki.client.exception.SekiInvalidTokenException
import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.auth.seki.client.model.Name
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import io.mockk.clearAllMocks
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@WebFluxTest(SimpleWebfluxController::class)
@Import(SharedWebfluxConfigurationReference::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleWebfluxControllerTest(
    @Autowired val webTestClient: WebTestClient
) {
    private val simpleResponse = """{"a": "foo", "b": "bar"}"""

    @MockkBean
    private lateinit var mockSekiClient: SekiClient

    @BeforeEach
    fun reset() {
        SimpleWebfluxController.receivedAuth = null
        clearAllMocks()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun testNoAuth() {
        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun testUnsuccessfulAuth() {
        val pretendSekiToken = UUID.randomUUID().toString()
        every { mockSekiClient.validate(pretendSekiToken) } throws SekiClientException("Unknown Exception")

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .header(HttpHeaders.AUTHORIZATION, "$AUTH_HEADER_VALUE_PREFIX$pretendSekiToken")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun testInvalidToken() {
        val pretendSekiToken = UUID.randomUUID().toString()
        every { mockSekiClient.validate(pretendSekiToken) } throws SekiInvalidTokenException("Bad token, bad token!")

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .header(HttpHeaders.AUTHORIZATION, "$AUTH_HEADER_VALUE_PREFIX$pretendSekiToken")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun testSuccessfulAuth() {
        val pretendSekiToken = UUID.randomUUID().toString()
        val authResponse = AuthResponse(
            User(
                id = "userid",
                tenantId = "apposnd",
                name = Name("Pearlene", "Pearlene Herrera", "Herrera")
            ),
            UserSession()
        )
        every { mockSekiClient.validate(pretendSekiToken) } returns authResponse

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .header(HttpHeaders.AUTHORIZATION, "$AUTH_HEADER_VALUE_PREFIX$pretendSekiToken")
            .exchange()
            .expectStatus().isOk
            .expectBody().json(simpleResponse)
        assertExpectedAuthReceived(authResponse, pretendSekiToken)
    }

    private fun assertExpectedAuthReceived(authResponse: AuthResponse, expectedToken: String) {
        when (val body = SimpleWebfluxController.receivedAuth) {
            is RoninAuthentication -> {
                assertThat(body.tenantId).isEqualTo(authResponse.user.tenantId)
                assertThat(body.userId).isEqualTo(authResponse.user.id)
                assertThat(body.udpId).isEqualTo(authResponse.user.udpId)
                assertThat(body.userFirstName).isEqualTo(authResponse.user.name.firstName)
                assertThat(body.userLastName).isEqualTo(authResponse.user.name.lastName)
                assertThat(body.userFullName).isEqualTo(authResponse.user.name.fullName)
                assertThat(body.tokenValue).isEqualTo(expectedToken)
            }

            else -> fail("Auth not passed")
        }
    }
}
