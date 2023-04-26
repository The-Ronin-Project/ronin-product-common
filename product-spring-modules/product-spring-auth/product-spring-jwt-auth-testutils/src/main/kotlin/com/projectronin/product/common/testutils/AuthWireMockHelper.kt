@file:Suppress("ktlint:no-wildcard-imports")

package com.projectronin.product.common.testutils

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

    fun setupMockAuthServerWithRsaKey(rsaKey: RSAKey, issuerHost: String = "http://127.0.0.1:$wireMockPort", issuerPath: String = "") {
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

    fun generateToken(rsaKey: RSAKey, issuer: String, claimSetCustomizer: (JWTClaimsSet.Builder) -> JWTClaimsSet.Builder = { it }): String =
        AuthMockHelper.generateToken(rsaKey, issuer, claimSetCustomizer)

    fun generateSekiToken(secret: String = sekiSharedSecret, user: String = UUID.randomUUID().toString(), tenantId: String = "ejh3j95h"): String =
        AuthMockHelper.generateSekiToken(secret, user, tenantId)

    fun secretKey(key: String): SecretKey = AuthMockHelper.secretKey(key)
}
