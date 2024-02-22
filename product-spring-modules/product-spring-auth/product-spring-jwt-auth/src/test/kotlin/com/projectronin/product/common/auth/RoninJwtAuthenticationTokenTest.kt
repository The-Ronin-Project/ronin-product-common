package com.projectronin.product.common.auth

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.resetToDefault
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.nimbusds.jwt.JWTClaimsSet
import com.projectronin.auth.RoninAuthentication
import com.projectronin.auth.token.RoninAuthenticationScheme
import com.projectronin.auth.token.RoninAuthenticationSchemeType
import com.projectronin.auth.token.RoninClaims
import com.projectronin.auth.token.RoninLoginProfile
import com.projectronin.auth.token.RoninName
import com.projectronin.auth.token.RoninUser
import com.projectronin.auth.token.RoninUserIdentity
import com.projectronin.auth.token.RoninUserIdentityType
import com.projectronin.auth.token.RoninUserType
import com.projectronin.test.jwt.generateRandomRsa
import com.projectronin.test.jwt.roninClaim
import com.projectronin.test.jwt.withAuthWiremockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

class RoninJwtAuthenticationTokenTest {

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

    @Test
    fun `should be successful with valid token`() {
        val (authToken, tokenString) = validRoninAuthenticationTokenAndRawToken()
        assertThat(authToken).isNotNull
        assertThat(authToken.tenantId).isEqualTo("")
        assertThat(authToken.userId).isEqualTo("")
        assertThat(authToken.udpId).isNull()
        assertThat(authToken.userFirstName).isEqualTo("")
        assertThat(authToken.userLastName).isEqualTo("")
        assertThat(authToken.userFullName).isEqualTo("")
        assertThat(authToken.roninClaims).isNotNull
        assertThat(authToken.roninClaims.user).isNull()
        assertThat(authToken.tokenValue).isEqualTo(tokenString)
    }

    @Test
    fun `should successfully construct ronin claims`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "9bc3abc9-d44d-4355-b81d-57e76218a954",
                userType = RoninUserType.Provider,
                name = RoninName(
                    fullText = "Jennifer Przepiora",
                    familyName = "Przepiora",
                    givenName = listOf("Jennifer"),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Los_Angeles",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingPatientUdpId = "apposnd-231982009",
                    accessingProviderUdpId = "apposnd-eSC7e62xM4tbHbRbARdo0kw3",
                    accessingExternalPatientId = "231982009"
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.ProviderUdpId,
                        tenantId = "apposnd",
                        id = "apposnd-231982009"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.SmartOnFhir,
                        tenantId = "apposnd",
                        id = "231982009"
                    )
                )
            )
        )

        val authValue = validRoninAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }
        assertThat(authValue).isInstanceOfAny(RoninJwtAuthenticationToken::class.java)
        assertThat(authValue.roninClaims).isNotNull
        assertThat(authValue.roninClaims).usingRecursiveComparison().isEqualTo(roninClaims)

        assertThat(authValue).isNotNull
        assertThat(authValue.tenantId).isEqualTo("apposnd")
        assertThat(authValue.userId).isEqualTo("9bc3abc9-d44d-4355-b81d-57e76218a954")
        assertThat(authValue.udpId).isEqualTo("apposnd-eSC7e62xM4tbHbRbARdo0kw3")
        assertThat(authValue.userFirstName).isEqualTo("Jennifer")
        assertThat(authValue.userLastName).isEqualTo("Przepiora")
        assertThat(authValue.userFullName).isEqualTo("Jennifer Przepiora")
    }

    @Test
    fun `should successfully construct ronin claims with patient udp id`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "9bc3abc9-d44d-4355-b81d-57e76218a954",
                userType = RoninUserType.Patient,
                name = RoninName(
                    fullText = "Jennifer Przepiora",
                    familyName = "Przepiora",
                    givenName = listOf("Jennifer"),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Los_Angeles",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingPatientUdpId = "apposnd-231982009",
                    accessingProviderUdpId = "apposnd-eSC7e62xM4tbHbRbARdo0kw3",
                    accessingExternalPatientId = "231982009"
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.ProviderUdpId,
                        tenantId = "apposnd",
                        id = "apposnd-231982009"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.SmartOnFhir,
                        tenantId = "apposnd",
                        id = "231982009"
                    )
                )
            )
        )

        val authValue = validRoninAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }
        assertThat(authValue.udpId).isEqualTo("apposnd-231982009")
    }

    private fun validRoninAuthenticationToken(claimSetCustomizer: (JWTClaimsSet.Builder) -> JWTClaimsSet.Builder = { it }): RoninAuthentication {
        return validRoninAuthenticationTokenAndRawToken(claimSetCustomizer).first
    }

    private fun validRoninAuthenticationTokenAndRawToken(claimSetCustomizer: (JWTClaimsSet.Builder) -> JWTClaimsSet.Builder = { it }): Pair<RoninAuthentication, String> {
        return withAuthWiremockServer(generateRandomRsa(), wireMockServer.baseUrl()) {
            val decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build()

            val token = jwtAuthToken {
                withTokenCustomizer {
                    claimSetCustomizer(roninClaim(RoninClaims(null)))
                }
            }

            val authToken = RoninCustomAuthenticationConverter().convert(decoder.decode(token))

            assertThat(authToken).isInstanceOf(RoninAuthentication::class.java)
            authToken as RoninAuthentication to token
        }
    }
}
