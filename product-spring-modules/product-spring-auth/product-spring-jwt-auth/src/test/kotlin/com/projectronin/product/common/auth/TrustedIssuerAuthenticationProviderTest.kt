package com.projectronin.product.common.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.resetToDefault
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.config.JwtSecurityProperties
import com.projectronin.product.common.config.SEKI_ISSUER_NAME
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
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import java.util.UUID

class TrustedIssuerAuthenticationProviderTest {

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
    fun tearDown() {
        resetToDefault()
    }

    @Test
    fun `should not fail instantiation if nothing is available`() {
        val resolver = TrustedIssuerAuthenticationProvider(
            securityProperties = invalidProperties,
            sekiClient = validSekiClient()
        )
        assertThatThrownBy { resolver.resolve(invalidProperties.issuers.first()) }
            .isInstanceOf(Exception::class.java)
    }

    @Test
    fun `should not accept an untrusted issuer`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val resolver = TrustedIssuerAuthenticationProvider(
                securityProperties = validProperties,
                sekiClient = validSekiClient()
            )
            assertThat(resolver.resolve("https://example.org")).isNull()
        }
    }

    @Test
    fun `should produce a valid authentication manager`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val resolver = TrustedIssuerAuthenticationProvider(
                securityProperties = validProperties,
                sekiClient = validSekiClient()
            )
            val authManager = resolver.resolve(validProperties.issuers.first())
            assertThat(authManager).isNotNull

            val token = jwtAuthToken()

            assertThat(authManager!!.resolve(BearerTokenAuthenticationToken(token))).isNotNull

            // while we're at it, let's verify some more tokens and make sure the JWT configs are not re-retrieved.
            verify(
                1,
                getRequestedFor(urlPathEqualTo("/oauth2/jwks"))
            )
            verify(
                1,
                getRequestedFor(urlPathEqualTo("/.well-known/openid-configuration"))
            )
            run {
                val anotherToken = jwtAuthToken()

                assertThat(authManager.resolve(BearerTokenAuthenticationToken(anotherToken))).isNotNull
            }
            run {
                val anotherToken = jwtAuthToken()

                assertThat(authManager.resolve(BearerTokenAuthenticationToken(anotherToken))).isNotNull
            }
            verify(
                1,
                getRequestedFor(urlPathEqualTo("/oauth2/jwks"))
            )
            verify(
                1,
                getRequestedFor(urlPathEqualTo("/.well-known/openid-configuration"))
            )
        }
    }

    @Test
    fun `should produce a valid manager for token`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val resolver = TrustedIssuerAuthenticationProvider(
                securityProperties = validProperties,
                sekiClient = validSekiClient()
            )
            val token = jwtAuthToken()

            val authManager = resolver.forToken(token)
            assertThat(authManager).isNotNull
            assertThat(authManager!!.resolve(BearerTokenAuthenticationToken(token))).isNotNull
        }
    }

    @Test
    fun `should produce null with token that doesn't have an issuer`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val resolver = TrustedIssuerAuthenticationProvider(
                securityProperties = validProperties,
                sekiClient = validSekiClient()
            )
            val token = jwtAuthToken {
                withTokenCustomizer { issuer(null) }
            }

            val authManager = resolver.forToken(token)
            assertThat(authManager).isNull()
        }
    }

    @Test
    fun `should produce a valid Seki authentication manager`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val resolver = TrustedIssuerAuthenticationProvider(
                securityProperties = validProperties,
                sekiClient = validSekiClient()
            )
            val authManager = resolver.resolve(SEKI_ISSUER_NAME)
            assertThat(authManager).isNotNull

            val userId = UUID.randomUUID().toString()
            val token = generateSekiToken(sekiSharedSecret, userId)
            SimpleSekiMock.successfulValidate(SekiResponseBuilder(token))

            assertThat(authManager!!.resolve(BearerTokenAuthenticationToken(token))).isNotNull
        }
    }

    @Test
    fun `should be be able to process a seki token without`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val resolver = TrustedIssuerAuthenticationProvider(
                securityProperties = validProperties.copy(sekiSharedSecret = null),
                sekiClient = validSekiClient()
            )
            val authManager = resolver.resolve(SEKI_ISSUER_NAME)
            assertThat(authManager).isNotNull

            val userId = UUID.randomUUID().toString()
            val token = generateSekiToken(sekiSharedSecret, userId)
            SimpleSekiMock.successfulValidate(SekiResponseBuilder(token))

            assertThat(authManager!!.resolve(BearerTokenAuthenticationToken(token))).isNotNull
        }
    }

    @Test
    fun `should not fail instantiation on empty seki data`() {
        TrustedIssuerAuthenticationProvider(
            securityProperties = validProperties
                .copy(sekiSharedSecret = null),
            sekiClient = validSekiClient()
        )
        TrustedIssuerAuthenticationProvider(
            securityProperties = validProperties,
            sekiClient = null
        )
    }

    @Test
    fun `should fail seki resolution if not configured`() {
        assertThatThrownBy {
            TrustedIssuerAuthenticationProvider(
                securityProperties = validProperties,
                sekiClient = null
            ).resolve(SEKI_ISSUER_NAME)
        }.hasMessageContaining("Seki client not configured: cannot validate seki tokens")
    }

    private fun validSekiClient() = SekiClient(
        "http://localhost:${wireMockServer.port()}/seki",
        OkHttpClient.Builder().build(),
        ObjectMapper().apply {
            findAndRegisterModules()
        }
    )

    private val validProperties
        get() = JwtSecurityProperties(
            issuers = listOf("http://localhost:${wireMockServer.port()}", SEKI_ISSUER_NAME, "http://localhost:${wireMockServer.port()}/second-issuer"),
            sekiSharedSecret = sekiSharedSecret,
            securedPathPatterns = listOf("/api/**")
        )

    private val invalidProperties
        get() = JwtSecurityProperties(
            issuers = listOf("http://localhost:${wireMockServer.port() - 1000}", SEKI_ISSUER_NAME, "http://localhost:${wireMockServer.port() - 1000}/second-issuer"),
            sekiSharedSecret = sekiSharedSecret,
            securedPathPatterns = listOf("/api/**")
        )
}
