package com.projectronin.product.common.config.sekimvccontrollertests

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
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(SimpleController::class)
@Import(SharedConfigurationReference::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleControllerTest(
    @Autowired val mockMvc: MockMvc
) {

    @MockkBean
    private lateinit var mockSekiClient: SekiClient

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun testFailedAuth() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/test/sample-object")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
    }

    @Test
    fun testAuthException() {
        every { mockSekiClient.validate("FOO") } throws SekiClientException("FOO")
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/test/sample-object")
                .header(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
    }

    @Test
    fun testAuthBadCredentialsException() {
        every { mockSekiClient.validate("FOO") } throws SekiInvalidTokenException("FOO")
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/test/sample-object")
                .header(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
    }

    @Test
    fun testSuccessfulAuth() {
        every { mockSekiClient.validate("FOO") } returns AuthResponse(
            User(
                id = "userId123",
                tenantId = "tenantId456",
                name = Name("John", "John Doe", "Doe")
            ),
            UserSession()
        )
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/test/sample-object")
                .header(HttpHeaders.AUTHORIZATION, "Bearer FOO")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }

    @Test
    fun testSuccessfulCookieAuth() {
        every { mockSekiClient.validate("BAR") } returns AuthResponse(
            User(
                id = "userId123",
                tenantId = "tenantId456",
                name = Name("John", "John Doe", "Doe")
            ),
            UserSession()
        )
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/test/sample-object")
                .header("x-state", "1234567")
                .cookie(Cookie("user_session_token_1234567", "BAR"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }
}
