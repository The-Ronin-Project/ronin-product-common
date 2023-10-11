package com.projectronin.product.common.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.config.JwtSecurityProperties
import com.projectronin.product.common.config.SEKI_ISSUER_NAME
import com.projectronin.product.common.testutils.AuthWireMockHelper
import com.projectronin.product.contracttest.wiremocks.SekiResponseBuilder
import com.projectronin.product.contracttest.wiremocks.SimpleSekiMock
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import java.util.UUID

class TrustedIssuerAuthenticationProviderTest {

    companion object {
        @JvmStatic
        @AfterAll
        fun stopWireMock() {
            AuthWireMockHelper.stop()
        }
    }

    @BeforeEach
    fun setup() {
        AuthWireMockHelper.start()
        AuthWireMockHelper.reset()
    }

    @AfterEach
    fun tearDown() {
        AuthWireMockHelper.reset()
    }

    @Test
    fun `should not fail instantiation if nothing is available`() {
        AuthWireMockHelper.stop()
        val resolver = TrustedIssuerAuthenticationProvider(
            securityProperties = AuthWireMockHelper.validProperties,
            sekiClient = validSekiClient()
        )
        assertThatThrownBy { resolver.resolve(AuthWireMockHelper.validProperties.issuers.first()) }
            .isInstanceOf(Exception::class.java)
    }

    @Test
    fun `should not accept an untrusted issuer`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)
        val resolver = TrustedIssuerAuthenticationProvider(
            securityProperties = AuthWireMockHelper.validProperties,
            sekiClient = validSekiClient()
        )
        assertThat(resolver.resolve("https://example.org")).isNull()
    }

    @Test
    fun `should produce a valid authentication manager`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)
        val resolver = TrustedIssuerAuthenticationProvider(
            securityProperties = AuthWireMockHelper.validProperties,
            sekiClient = validSekiClient()
        )
        val authManager = resolver.resolve(AuthWireMockHelper.validProperties.issuers.first())
        assertThat(authManager).isNotNull

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

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
            val anotherToken = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

            assertThat(authManager.resolve(BearerTokenAuthenticationToken(anotherToken))).isNotNull
        }
        run {
            val anotherToken = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

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

    @Test
    fun `should produce a valid manager for token`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)
        val resolver = TrustedIssuerAuthenticationProvider(
            securityProperties = AuthWireMockHelper.validProperties,
            sekiClient = validSekiClient()
        )
        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        val authManager = resolver.forToken(token)
        assertThat(authManager).isNotNull
        assertThat(authManager!!.resolve(BearerTokenAuthenticationToken(token))).isNotNull
    }

    @Test
    fun `should produce null with token that doesn't have an issuer`() {
        val resolver = TrustedIssuerAuthenticationProvider(
            securityProperties = AuthWireMockHelper.validProperties,
            sekiClient = validSekiClient()
        )
        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "foo") { builder ->
            builder.issuer(null)
        }

        val authManager = resolver.forToken(token)
        assertThat(authManager).isNull()
    }

    @Test
    fun `should produce a valid Seki authentication manager`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)
        val resolver = TrustedIssuerAuthenticationProvider(
            securityProperties = AuthWireMockHelper.validProperties,
            sekiClient = validSekiClient()
        )
        val authManager = resolver.resolve(SEKI_ISSUER_NAME)
        assertThat(authManager).isNotNull

        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId)
        SimpleSekiMock.successfulValidate(SekiResponseBuilder(token))

        assertThat(authManager!!.resolve(BearerTokenAuthenticationToken(token))).isNotNull
    }

    @Test
    fun `should be be able to process a seki token without`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)
        val resolver = TrustedIssuerAuthenticationProvider(
            securityProperties = AuthWireMockHelper.validProperties.copy(sekiSharedSecret = null),
            sekiClient = validSekiClient()
        )
        val authManager = resolver.resolve(SEKI_ISSUER_NAME)
        assertThat(authManager).isNotNull

        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId)
        SimpleSekiMock.successfulValidate(SekiResponseBuilder(token))

        assertThat(authManager!!.resolve(BearerTokenAuthenticationToken(token))).isNotNull
    }

    @Test
    fun `should not fail instantiation on empty seki data`() {
        TrustedIssuerAuthenticationProvider(
            securityProperties = AuthWireMockHelper.validProperties
                .copy(sekiSharedSecret = null),
            sekiClient = validSekiClient()
        )
        TrustedIssuerAuthenticationProvider(
            securityProperties = AuthWireMockHelper.validProperties,
            sekiClient = null
        )
    }

    @Test
    fun `should fail seki resolution if not configured`() {
        assertThatThrownBy {
            TrustedIssuerAuthenticationProvider(
                securityProperties = AuthWireMockHelper.validProperties,
                sekiClient = null
            ).resolve(SEKI_ISSUER_NAME)
        }.hasMessageContaining("Seki client not configured: cannot validate seki tokens")
    }

    private fun validSekiClient() = SekiClient(
        "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}/seki",
        OkHttpClient.Builder().build(),
        ObjectMapper().apply {
            findAndRegisterModules()
        }
    )

    private val AuthWireMockHelper.validProperties
        get() = JwtSecurityProperties(
            securedPathPatterns = listOf("/api/**"),
            detailedErrors = true,
            issuers = listOf("http://127.0.0.1:$wireMockPort", SEKI_ISSUER_NAME, "http://127.0.0.1:$wireMockPort/second-issuer"),
            sekiSharedSecret = sekiSharedSecret
        )
}
