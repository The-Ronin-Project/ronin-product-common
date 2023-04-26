package com.projectronin.product.common.auth

import com.github.tomakehurst.wiremock.client.WireMock
import com.projectronin.product.common.testutils.AuthWireMockHelper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.util.UUID

class RoninCustomAuthenticationConverterTest {

    companion object {

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            AuthWireMockHelper.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            AuthWireMockHelper.stop()
        }
    }

    @BeforeEach
    fun setup() {
        WireMock.resetToDefault()
    }

    @AfterEach
    fun teardown() {
        WireMock.resetToDefault()
    }

    @Test
    fun `should fail on seki issuer`() {
        val decoder = NimbusJwtDecoder.withSecretKey(AuthWireMockHelper.secretKey(AuthWireMockHelper.sekiSharedSecret)).build()

        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId)

        assertThatThrownBy { RoninCustomAuthenticationConverter().convert(decoder.decode(token)) }
            .isInstanceOf(BadCredentialsException::class.java)
    }

    @Test
    fun `should succeed with jwt key with scopes`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val decoder = NimbusJwtDecoder.withPublicKey(AuthWireMockHelper.rsaKey.toRSAPublicKey()).build()

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}") { builder ->
            builder
                .claim("scope", "session:read phone_user:create phone_user:update thing_requiring_scope:read")
        }

        val authToken = RoninCustomAuthenticationConverter().convert(decoder.decode(token))

        assertThat(authToken).isInstanceOf(RoninAuthentication::class.java)
        val roninAuthToken = authToken as RoninAuthentication
        assertThat(roninAuthToken.isAuthenticated).isTrue
        assertThat(roninAuthToken.roninClaims).isNotNull
        val authorities = roninAuthToken.authorities
        assertThat(authorities).containsExactlyInAnyOrder(
            SimpleGrantedAuthority("SCOPE_session:read"),
            SimpleGrantedAuthority("SCOPE_phone_user:create"),
            SimpleGrantedAuthority("SCOPE_phone_user:update"),
            SimpleGrantedAuthority("SCOPE_thing_requiring_scope:read")
        )
    }

    @Test
    fun `should succeed with jwt key without scopes`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val decoder = NimbusJwtDecoder.withPublicKey(AuthWireMockHelper.rsaKey.toRSAPublicKey()).build()

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        val authToken = RoninCustomAuthenticationConverter().convert(decoder.decode(token))

        assertThat(authToken).isInstanceOf(RoninAuthentication::class.java)
        val roninAuthToken = authToken as RoninAuthentication
        assertThat(roninAuthToken.isAuthenticated).isTrue
        assertThat(roninAuthToken.roninClaims).isNotNull
        val authorities = roninAuthToken.authorities
        assertThat(authorities).isEmpty()
    }
}
