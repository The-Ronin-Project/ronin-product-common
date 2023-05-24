@file:Suppress("ktlint:no-wildcard-imports")

package com.projectronin.product.common.testutils

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.resetToDefault
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.projectronin.product.common.auth.token.RoninAuthenticationScheme
import com.projectronin.product.common.auth.token.RoninAuthenticationSchemeType
import com.projectronin.product.common.auth.token.RoninClaims
import com.projectronin.product.common.auth.token.RoninLoginProfile
import com.projectronin.product.common.auth.token.RoninName
import com.projectronin.product.common.auth.token.RoninUser
import com.projectronin.product.common.auth.token.RoninUserIdentity
import com.projectronin.product.common.auth.token.RoninUserIdentityType
import com.projectronin.product.common.auth.token.RoninUserType
import com.projectronin.product.common.config.JsonProvider
import org.springframework.test.util.TestSocketUtils
import java.util.UUID
import javax.crypto.SecretKey

object AuthWireMockHelper {

    val wireMockPort = TestSocketUtils.findAvailableTcpPort()

    private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(wireMockPort))

    val rsaKey: RSAKey = AuthMockHelper.rsaKey

    val sekiSharedSecret = AuthMockHelper.sekiSharedSecret
    val defaultSekiToken = AuthMockHelper.defaultSekiToken

    val isRunning: Boolean
        get() = wireMockServer.isRunning

    fun start() {
        if (!isRunning) {
            wireMockServer.start()
            configureFor(wireMockPort)
        }
    }

    fun reset() {
        if (isRunning) {
            resetToDefault()
        }
    }

    fun stop() {
        if (isRunning) {
            wireMockServer.stop()
        }
    }

    fun defaultIssuer(): String = "http://127.0.0.1:$wireMockPort"

    fun setupMockAuthServerWithRsaKey(rsaKey: RSAKey = AuthMockHelper.rsaKey, issuerHost: String = defaultIssuer(), issuerPath: String = "") {
        val jwks = JWKSet(listOf(rsaKey, AuthKeyGenerator.generateRandomRsa()))

        // language=json
        val openidConfiguration = """
                {
                    "issuer": "$issuerHost$issuerPath",
                    "authorization_endpoint": "$issuerHost$issuerPath/oauth2/authorize",
                    "token_endpoint": "$issuerHost$issuerPath/oauth2/token",
                    "token_endpoint_auth_methods_supported": [
                        "client_secret_basic",
                        "client_secret_post",
                        "client_secret_jwt",
                        "private_key_jwt"
                    ],
                    "jwks_uri": "$issuerHost$issuerPath/oauth2/jwks",
                    "userinfo_endpoint": "$issuerHost$issuerPath/userinfo",
                    "response_types_supported": [
                        "code"
                    ],
                    "grant_types_supported": [
                        "authorization_code",
                        "client_credentials",
                        "refresh_token"
                    ],
                    "revocation_endpoint": "$issuerHost$issuerPath/oauth2/revoke",
                    "revocation_endpoint_auth_methods_supported": [
                        "client_secret_basic",
                        "client_secret_post",
                        "client_secret_jwt",
                        "private_key_jwt"
                    ],
                    "introspection_endpoint": "$issuerHost$issuerPath/oauth2/introspect",
                    "introspection_endpoint_auth_methods_supported": [
                        "client_secret_basic",
                        "client_secret_post",
                        "client_secret_jwt",
                        "private_key_jwt"
                    ],
                    "subject_types_supported": [
                        "public"
                    ],
                    "id_token_signing_alg_values_supported": [
                        "RS256"
                    ],
                    "scopes_supported": [
                        "openid"
                    ]
                }
        """.trimIndent()

        stubFor(
            get(urlPathMatching("$issuerPath/oauth2/jwks"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(AuthKeyGenerator.createJWKSForPublicDisplay(jwks))
                )
        )

        stubFor(
            get(urlPathMatching("$issuerPath/.well-known/openid-configuration"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(openidConfiguration)
                )
        )
    }

    fun generateToken(rsaKey: RSAKey = AuthMockHelper.rsaKey, issuer: String = defaultIssuer(), claimSetCustomizer: (JWTClaimsSet.Builder) -> JWTClaimsSet.Builder = { it }): String =
        AuthMockHelper.generateToken(rsaKey, issuer, claimSetCustomizer)

    fun generateTokenWithRoninClaims(
        rsaKey: RSAKey = AuthMockHelper.rsaKey,
        issuer: String = defaultIssuer(),
        roninClaims: RoninClaims = defaultRoninClaims(),
        claimSetCustomizer: (JWTClaimsSet.Builder) -> JWTClaimsSet.Builder = { it }
    ): String {
        val claims: Map<String, Any> = JsonProvider.objectMapper.readValue(
            JsonProvider.objectMapper.writeValueAsString(
                roninClaims
            )
        )

        return AuthMockHelper.generateToken(rsaKey, issuer) { builder ->
            claimSetCustomizer(
                builder.claim(
                    "urn:projectronin:authorization:claims:version:1",
                    claims
                )
            )
        }
    }

    fun generateSekiToken(secret: String = sekiSharedSecret, user: String = UUID.randomUUID().toString(), tenantId: String = "ejh3j95h"): String =
        AuthMockHelper.generateSekiToken(secret, user, tenantId)

    fun secretKey(key: String): SecretKey = AuthMockHelper.secretKey(key)

    fun defaultRoninClaims(
        id: String = "9bc3abc9-d44d-4355-b81d-57e76218a954",
        userType: RoninUserType = RoninUserType.Provider,
        fullName: String = "Jennifer Przepiora",
        familyName: String? = "Przepiora",
        givenName: String? = "Jennifer",
        tenantId: String? = "apposnd",
        patientUdpId: String? = "apposnd-231982009",
        patientFhirId: String? = "231982009",
        providerUdpId: String? = "apposnd-eSC7e62xM4tbHbRbARd1o0kw3",
        providerFhirId: String? = "231982009",
        preferredTimeZone: String? = "America/Los_Angeles",
        identities: List<RoninUserIdentity> = listOf(
            RoninUserIdentity(
                type = RoninUserIdentityType.ProviderUdpId,
                tenantId = tenantId,
                id = providerUdpId
            )
        ),
        authenticationSchemes: List<RoninAuthenticationScheme> = listOf(
            RoninAuthenticationScheme(
                type = RoninAuthenticationSchemeType.SmartOnFhir,
                tenantId = tenantId,
                id = providerFhirId
            )
        )
    ): RoninClaims {
        return RoninClaims(
            user = RoninUser(
                id = id,
                userType = userType,
                name = RoninName(
                    fullText = fullName,
                    familyName = familyName,
                    givenName = givenName?.let { listOf(it) } ?: emptyList(),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = preferredTimeZone,
                loginProfile = RoninLoginProfile(
                    accessingTenantId = tenantId,
                    accessingPatientUdpId = patientUdpId,
                    accessingProviderUdpId = providerUdpId,
                    accessingExternalPatientId = patientFhirId
                ),
                identities = identities,
                authenticationSchemes = authenticationSchemes
            )
        )
    }
}
