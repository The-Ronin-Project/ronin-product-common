@file:Suppress("ktlint:no-wildcard-imports")

package com.projectronin.product.common.jwtmvccontrollertests

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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(JwtWebMVCSimpleController::class)
@Import(JwtWebMVCSharedConfigurationReference::class, AudiencePropertiesConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtWebMVCSimpleControllerWithAudienceVerificationTest(
    @Autowired val mockMvc: MockMvc,
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
    fun `should fail with wrong audience`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val token = jwtAuthToken {
                withAudience("https://example.org")
            }

            val result = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/test/sample-object")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andReturn()
            verifyUnauthorizedBody(result)
        }
    }

    @Test
    fun `should fail with no audience`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val token = jwtAuthToken()

            val result = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/test/sample-object")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andReturn()
            verifyUnauthorizedBody(result)
        }
    }

    @Test
    fun `should be successful with valid audience`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val token = jwtAuthToken {
                withAudience("http://localhost:${wireMockServer.port()}")
            }

            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/test/sample-object")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

            assertThat(authHolderBean.latestRoninAuth).isNotNull
            val authValue = authHolderBean.latestRoninAuth!!
            assertThat(authValue).isInstanceOfAny(RoninJwtAuthenticationToken::class.java)
        }
    }

    private fun verifyUnauthorizedBody(result: MvcResult) {
        val body: ErrorResponse = objectMapper.readValue(result.response.contentAsString)

        assertThat(body.httpStatus).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(body.timestamp).isNotNull
        assertThat(body.status).isEqualTo(401)
        assertThat(body.message).isEqualTo("Authentication Error")
    }
}
