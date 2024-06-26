package com.projectronin.product.common.auth

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.resetToDefault
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.projectronin.auth.RoninAuthentication
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.contracttest.wiremocks.SekiResponseBuilder
import com.projectronin.product.contracttest.wiremocks.SimpleSekiMock
import com.projectronin.test.jwt.generateRandomRsa
import com.projectronin.test.jwt.withAuthWiremockServer
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.util.UUID

class SekiCustomAuthenticationConverterTest {

    companion object {
        private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun staticSetup() {
            wireMockServer.start()
            configureFor(wireMockServer.port())
        }

        @AfterAll
        @JvmStatic
        fun staticTeardown() {
            wireMockServer.stop()
        }
    }

    @BeforeEach
    fun setup() {
        resetToDefault()
    }

    @AfterEach
    fun teardown() {
        resetToDefault()
    }

    val sekiClient = SekiClient(
        "http://127.0.0.1:${wireMockServer.port()}/seki/session/validate",
        OkHttpClient.Builder().build(),
        JsonProvider.objectMapper
    )

    @Test
    fun `should get a valid seki token`() {
        val decoder = NimbusJwtDecoder.withSecretKey(secretKey(sekiSharedSecret)).build()

        val userId = UUID.randomUUID().toString()
        val token = generateSekiToken(sekiSharedSecret, userId)

        val builder = SekiResponseBuilder(token)
            .firstName("foo")
            .lastName("bar")

        SimpleSekiMock.successfulValidate(builder)

        val authToken = SekiCustomAuthenticationConverter(sekiClient).convert(decoder.decode(token))
        assertThat(authToken).isNotNull
        assertThat(authToken).isInstanceOf(RoninAuthentication::class.java)
        val roninAuthentication = authToken as RoninAuthentication
        assertThat(roninAuthentication.isAuthenticated).isTrue
        assertThat(roninAuthentication.roninClaims).isNotNull
        assertThat(roninAuthentication.tenantId).isEqualTo(builder.tenantId)
        assertThat(roninAuthentication.userId).isEqualTo(builder.sekiUserId.toString())
        assertThat(roninAuthentication.udpId).isEqualTo(builder.udpId)
        assertThat(roninAuthentication.userFirstName).isEqualTo(builder.firstName)
        assertThat(roninAuthentication.userLastName).isEqualTo(builder.lastName)
        assertThat(roninAuthentication.userFullName).isEqualTo(builder.fullName)
        assertThat(roninAuthentication.roninClaims).isNotNull
    }

    @Test
    fun `should fail when not seki issuer`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build()

            val token = jwtAuthToken()

            assertThatThrownBy { SekiCustomAuthenticationConverter(sekiClient).convert(decoder.decode(token)) }
                .isInstanceOf(BadCredentialsException::class.java)
        }
    }
}
