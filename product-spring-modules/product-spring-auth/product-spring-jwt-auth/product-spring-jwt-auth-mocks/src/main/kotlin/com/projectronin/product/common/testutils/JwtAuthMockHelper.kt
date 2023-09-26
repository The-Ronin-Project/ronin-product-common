package com.projectronin.product.common.testutils

import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.auth.RoninAuthentication
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
import com.projectronin.product.common.auth.AuthenticationProvider
import com.projectronin.product.common.auth.IssuerAuthenticationProvider
import com.projectronin.product.common.auth.RoninJwtAuthenticationToken
import com.projectronin.product.common.config.JsonProvider
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import java.time.Instant

object JwtAuthMockHelper {

    const val defaultToken: String =
        "eyJraWQiOiJqQjk1VGUxbHFKb3dlWDhJSnd5R25YRUQ2aVZiS0tQTlQ1b0Fxa1grL2FzPSIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xOjUwMTYxIiwic3ViIjoiYWxpY2UiLCJ1cm46cHJvamVjdHJvbmluOmF1dGhvcml6YXRpb246Y2xhaW1zOnZlcnNpb246MSI6eyJ1c2VyIjp7ImlkIjoiOWJjM2FiYzktZDQ0ZC00MzU1LWI4MWQtNTdlNzYyMThhOTU0IiwidXNlclR5cGUiOiJQUk9WSURFUiIsIm5hbWUiOnsiZnVsbFRleHQiOiJKZW5uaWZlciBQcnplcGlvcmEiLCJmYW1pbHlOYW1lIjoiUHJ6ZXBpb3JhIiwiZ2l2ZW5OYW1lIjpbIkplbm5pZmVyIl0sInByZWZpeCI6W10sInN1ZmZpeCI6W119LCJsb2dpblByb2ZpbGUiOnsiYWNjZXNzaW5nVGVuYW50SWQiOiJhcHBvc25kIiwiYWNjZXNzaW5nUHJvdmlkZXJVZHBJZCI6ImFwcG9zbmQtZVNDN2U2MnhNNHRiSGJSYkFSZG8wa3czIiwiYWNjZXNzaW5nUGF0aWVudFVkcElkIjoiYXBwb3NuZC0yMzE5ODIwMDkifSwiaWRlbnRpdGllcyI6W3sidHlwZSI6IlBST1ZJREVSX1VEUF9JRCIsInRlbmFudElkIjoiYXBwb3NuZCIsImlkIjoiYXBwb3NuZC0yMzE5ODIwMDkifV0sImF1dGhlbnRpY2F0aW9uU2NoZW1lcyI6W3sidHlwZSI6IlNNQVJUX09OX0ZISVIiLCJ0ZW5hbnRJZCI6ImFwcG9zbmQiLCJpZCI6IjIzMTk4MjAwOSJ9XX19LCJpYXQiOjE2ODI0NTIxNzl9.qPwGSi5RgryXhmuKmFv9yFEBsi5uEkQ0mKb_9I1T41f9s0ZfVX-KDeWmMWgZvXS9RAi-2qQhRe_6F6fzg-zn0Ezgs1sDsdI4lHrSZqUL6HsmcYbcXt5BXvpCjlnLv5a1eo_jDB270TI5AskbYNtUqvzR-SA-oPM4MQHNeGaLfStIlASMZJVW236Unr_tqYtlR6ucSfaTVBdw-9J31pY751HWBWbLUQ5zYxcj7WZQFEbfpo40xgIRuKB0QsX97Q4ufoWpTl52fNu2CLT104DwmLktMnDUvz4PHi9992P44Cd2Q8XXvbdpvn8HcM-WYOW0i0AVQ4wh7nRNBTk7oFVW2w"

    val defaultHeaders: Map<String, Any?> = mapOf(
        "kid" to "jB95Te1lqJoweX8IJwyGnXED6iVbKKPNT5oAqkX+/as=",
        "alg" to "RS256"
    )

    internal var currentAuthenticationProvider: AuthenticationProvider = createAuthenticationProvider()

    fun reset() {
        currentAuthenticationProvider = createAuthenticationProvider()
    }

    fun configure(authenticationProvider: AuthenticationProvider) {
        currentAuthenticationProvider = authenticationProvider
    }

    @Suppress("unused")
    fun createIssuerAuthenticationProvider(
        exceptionToThrow: Throwable? = null,
        authenticationSupplier: () -> RoninAuthentication? = { defaultAuthenticationToken() }
    ): IssuerAuthenticationProvider {
        return object : IssuerAuthenticationProvider {
            override fun resolve(issuerUrl: String?): AuthenticationProvider = object : AuthenticationProvider {
                override fun resolve(authentication: Authentication): Authentication? {
                    if (exceptionToThrow != null) {
                        throw exceptionToThrow
                    }
                    return authenticationSupplier()
                }
            }
        }
    }

    fun createAuthenticationProvider(
        exceptionToThrow: Throwable? = null,
        authenticationSupplier: () -> RoninAuthentication? = { defaultAuthenticationToken() }
    ): AuthenticationProvider {
        return object : AuthenticationProvider {
            override fun resolve(authentication: Authentication): Authentication? {
                if (exceptionToThrow != null) {
                    throw exceptionToThrow
                }
                return authenticationSupplier()
            }
        }
    }

    fun createAuthenticationProviderWithSpecificToken(
        roninClaims: RoninClaims,
        authenticationSupplier: () -> RoninAuthentication? = { defaultAuthenticationToken(roninClaims = roninClaims) }
    ): AuthenticationProvider {
        return object : AuthenticationProvider {
            override fun resolve(authentication: Authentication): Authentication? {
                return authenticationSupplier()
            }
        }
    }

    fun defaultAuthenticationToken(
        roninClaims: RoninClaims = defaultRoninClaims(),
        tokenValue: String = defaultToken,
        headers: Map<String, Any?> = defaultHeaders,
        claims: Map<String, Any?> = defaultClaims(roninClaims)
    ): RoninAuthentication {
        val jwt = Jwt.withTokenValue(tokenValue)
            .headers { h: MutableMap<String?, Any?> -> h.putAll(headers) }
            .claims { c: MutableMap<String?, Any?> -> c.putAll(claims) }
            .build()
        return RoninJwtAuthenticationToken(
            jwt,
            JwtGrantedAuthoritiesConverter().convert(jwt) ?: emptyList()
        )
    }

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

    fun defaultClaims(roninClaims: RoninClaims = defaultRoninClaims()): Map<String, Any?> {
        val roninClaimsMap: Map<String, Any> = JsonProvider.objectMapper.readValue(
            JsonProvider.objectMapper.writeValueAsString(
                roninClaims
            )
        )
        return mapOf(
            "iss" to "http://127.0.0.1:50161",
            "sub" to "alice",
            RoninClaimsAuthentication.roninClaimsKey to roninClaimsMap,
            "iat" to Instant.now()
        )
    }
}
