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
import com.projectronin.auth.RoninClaimsAuthentication
import com.projectronin.auth.token.RoninAuthenticationScheme
import com.projectronin.auth.token.RoninAuthenticationSchemeType
import com.projectronin.auth.token.RoninClaims
import com.projectronin.auth.token.RoninLoginProfile
import com.projectronin.auth.token.RoninName
import com.projectronin.auth.token.RoninUser
import com.projectronin.auth.token.RoninUserIdentity
import com.projectronin.auth.token.RoninUserIdentityType
import com.projectronin.auth.token.RoninUserType
import com.projectronin.product.common.config.JsonProvider
import org.springframework.test.util.TestSocketUtils
import java.util.Date
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

    fun setupMockAuthServerWithRsaKeyX(rsaKey: RSAKey = AuthMockHelper.rsaKey, issuerHost: String = defaultIssuer(), issuerPath: String = "") {
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

fun JWTClaimsSet.Builder.roninClaim(claims: RoninClaims) = apply {
    claim(
        RoninClaimsAuthentication.roninClaimsKey,
        JsonProvider.objectMapper.readValue(
            JsonProvider.objectMapper.writeValueAsString(
                claims
            )
        )
    )
}

/**
 * Configures a local wiremock server to handle JWT auth.  For example:
 *
 * ```
 * withAuthWiremockServer {
 *     val token = jwtAuthToken {
 *         withRsaKey(TEST_RSA_KEY)
 *             .withUserType(RoninUserType.RoninEmployee)
 *             .withScopes("admin:read", "admin:write", "tenant:delete")
 *     }
 *     // do some tests
 * }
 * ```
 *
 * Note that outside of contract testing, you should be using com.projectronin.product.common.testutils.JwtAuthMockHelper instead,
 * as this requires specialized setup.  See examples in com.projectronin.product.common.testconfigs.BasicPropertiesConfig and
 * com.projectronin.product.common.testconfigs.AudiencePropertiesConfig if you want to torture yourself.
 */
fun <T> withAuthWiremockServer(rsaKey: RSAKey = AuthMockHelper.rsaKey, issuerHost: String = AuthWireMockHelper.defaultIssuer(), issuerPath: String = "", block: WireMockServerContext.() -> T): T {
    return WireMockServerContext(rsaKey, issuerHost, issuerPath).use { block(it) }
}

class WireMockServerContext(private val rsaKey: RSAKey, private val issuerHost: String, private val issuerPath: String) : AutoCloseable {

    init {
        AuthWireMockHelper.setupMockAuthServerWithRsaKeyX(rsaKey, issuerHost, issuerPath)
    }

    /**
     * Returns a JWT auth token.  See `AuthWireMockHelper.defaultRoninClaims()` for the defaults
     * that get set into it.  You can pass a block that customizes the code, e.g.:
     *
     * ```
     * val token = jwtAuthToken {
     *     withRsaKey(TEST_RSA_KEY)
     *         .withUserType(RoninUserType.RoninEmployee)
     *         .withScopes("admin:read", "admin:write", "tenant:delete")
     * }
     * ```
     *
     * Note that outside of contract testing, you should be using com.projectronin.product.common.testutils.JwtAuthMockHelper instead,
     * as this requires specialized setup.  See examples in com.projectronin.product.common.testconfigs.BasicPropertiesConfig and
     * com.projectronin.product.common.testconfigs.AudiencePropertiesConfig if you want to torture yourself.
     */
    fun jwtAuthToken(block: RoninWireMockAuthenticationContext.() -> Unit = {}): String {
        return wiremockJwtAuthToken(block)
    }

    fun randomRsaKey(): RSAKey = AuthKeyGenerator.generateRandomRsa()

    fun defaultRsaKey(): RSAKey = AuthWireMockHelper.rsaKey

    fun defaultIssuerHost(): String = AuthWireMockHelper.defaultIssuer()

    fun withAnotherSever(rsaKey: RSAKey, issuerHost: String, issuerPath: String): WireMockServerContext {
        AuthWireMockHelper.setupMockAuthServerWithRsaKeyX(rsaKey, issuerHost, issuerPath)
        return this
    }

    override fun close() {
        AuthWireMockHelper.reset()
    }
}

/**
 * Returns a JWT auth token.  See `AuthWireMockHelper.defaultRoninClaims()` for the defaults
 * that get set into it.  You can pass a block that customizes the code, e.g.:
 *
 * ```
 * val token = wiremockJwtAuthToken {
 *     withRsaKey(TEST_RSA_KEY)
 *         .withUserType(RoninUserType.RoninEmployee)
 *         .withScopes("admin:read", "admin:write", "tenant:delete")
 * }
 * ```
 *
 * Note that outside of contract testing, you should be using com.projectronin.product.common.testutils.JwtAuthMockHelper instead,
 * as this requires specialized setup.  See examples in com.projectronin.product.common.testconfigs.BasicPropertiesConfig and
 * com.projectronin.product.common.testconfigs.AudiencePropertiesConfig if you want to torture yourself.
 */
fun wiremockJwtAuthToken(block: RoninWireMockAuthenticationContext.() -> Unit = {}): String {
    val ctx = RoninWireMockAuthenticationContext(AuthWireMockHelper.defaultRoninClaims().user!!)
    block(ctx)
    return ctx.buildToken()
}

class RoninWireMockAuthenticationContext(roninUser: RoninUser) {

    private var id: String = roninUser.id
    private var userType: RoninUserType = roninUser.userType
    private var name: RoninName? = roninUser.name
    private var preferredTimeZone: String? = roninUser.preferredTimeZone
    private var loginProfile: RoninLoginProfile? = roninUser.loginProfile
    private var identities: MutableList<RoninUserIdentity> = roninUser.identities.toMutableList()
    private var authenticationSchemes: MutableList<RoninAuthenticationScheme> = roninUser.authenticationSchemes.toMutableList()
    private var customizer: JWTClaimsSet.Builder.() -> JWTClaimsSet.Builder = { this }
    private var rsaKey: RSAKey = AuthMockHelper.rsaKey
    private var issuer: String = AuthWireMockHelper.defaultIssuer()
    private var subject: String = "alice"
    private var issuedAt: Date = Date()

    private var defaultClaims: Map<String, Any?> = mapOf()

    fun buildToken(): String {
        val roninClaims = RoninClaims(
            RoninUser(
                id = id,
                userType = userType,
                name = name,
                preferredTimeZone = preferredTimeZone,
                loginProfile = loginProfile,
                identities = identities,
                authenticationSchemes = authenticationSchemes
            )
        )
        return AuthMockHelper.generateToken(
            rsaKey = rsaKey,
            issuer = issuer
        ) {
            customizer(
                defaultClaims.entries.fold(it) { builder, entry ->
                    builder.claim(entry.key, entry.value)
                }
                    .roninClaim(roninClaims)
                    .issueTime(issuedAt)
            )
        }
    }

    fun withRsaKey(rsaKey: RSAKey): RoninWireMockAuthenticationContext {
        this.rsaKey = rsaKey
        return this
    }

    fun withScopes(vararg scope: String): RoninWireMockAuthenticationContext = withClaim("scope", scope.toList())

    fun withIssuer(issuer: String): RoninWireMockAuthenticationContext {
        this.issuer = issuer
        return this
    }

    fun withSubject(subject: String): RoninWireMockAuthenticationContext {
        this.subject = subject
        return this
    }

    fun withIat(issuedAt: Date): RoninWireMockAuthenticationContext {
        this.issuedAt = issuedAt
        return this
    }

    fun withAudience(aud: String): RoninWireMockAuthenticationContext = withClaim("aud", aud)

    //     val id: String,
    fun withUserId(id: String): RoninWireMockAuthenticationContext {
        this.id = id
        return this
    }

    fun withUserType(userType: RoninUserType): RoninWireMockAuthenticationContext {
        this.userType = userType
        return this
    }

    fun withName(name: RoninName?): RoninWireMockAuthenticationContext {
        this.name = name
        return this
    }

    fun withPreferredTimeZone(preferredTimeZone: String?): RoninWireMockAuthenticationContext {
        this.preferredTimeZone = preferredTimeZone
        return this
    }

    fun withLoginProfile(loginProfile: RoninLoginProfile?): RoninWireMockAuthenticationContext {
        this.loginProfile = loginProfile
        return this
    }

    fun withIdentities(vararg identities: RoninUserIdentity): RoninWireMockAuthenticationContext {
        this.identities += identities
        return this
    }

    fun withAuthenticationSchemes(vararg authenticationSchemes: RoninAuthenticationScheme): RoninWireMockAuthenticationContext {
        this.authenticationSchemes += authenticationSchemes
        return this
    }

    fun withTokenCustomizer(fn: JWTClaimsSet.Builder.() -> JWTClaimsSet.Builder): RoninWireMockAuthenticationContext {
        customizer = fn
        return this
    }

    fun withClaim(key: String, value: Any?): RoninWireMockAuthenticationContext {
        defaultClaims = defaultClaims + (key to value)
        return this
    }
}
