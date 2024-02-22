package com.projectronin.product.common.auth

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.projectronin.auth.RoninAuthentication
import com.projectronin.test.jwt.generateRandomRsa
import com.projectronin.test.jwt.withAuthWiremockServer
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
        private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun staticSetup() {
            wireMockServer.start()
            WireMock.configureFor(wireMockServer.port())
        }

        @AfterAll
        @JvmStatic
        fun staticTeardown() {
            wireMockServer.stop()
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
        val decoder = NimbusJwtDecoder.withSecretKey(secretKey(sekiSharedSecret)).build()

        val userId = UUID.randomUUID().toString()
        val token = generateSekiToken(sekiSharedSecret, userId)

        assertThatThrownBy { RoninCustomAuthenticationConverter().convert(decoder.decode(token)) }
            .isInstanceOf(BadCredentialsException::class.java)
    }

    @Test
    fun `should succeed with jwt key with scopes`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build()

            val token = jwtAuthToken {
                withScopes("session:read", "phone_user:create", "phone_user:update", "thing_requiring_scope:read")
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
    }

    @Test
    fun `should succeed with jwt key without scopes`() {
        withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build()

            val token = jwtAuthToken()

            val authToken = RoninCustomAuthenticationConverter().convert(decoder.decode(token))

            assertThat(authToken).isInstanceOf(RoninAuthentication::class.java)
            val roninAuthToken = authToken as RoninAuthentication
            assertThat(roninAuthToken.isAuthenticated).isTrue
            assertThat(roninAuthToken.roninClaims).isNotNull
            val authorities = roninAuthToken.authorities
            assertThat(authorities).isEmpty()
        }
    }
}
