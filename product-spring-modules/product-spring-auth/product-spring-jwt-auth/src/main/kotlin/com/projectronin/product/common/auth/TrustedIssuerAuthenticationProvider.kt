package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.config.JwtSecurityProperties
import com.projectronin.product.common.config.SEKI_ISSUER_NAME
import mu.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.spec.SecretKeySpec

@FunctionalInterface
interface AuthenticationProvider {
    fun resolve(authentication: Authentication): Authentication?
}

@FunctionalInterface
interface IssuerAuthenticationProvider {
    fun resolve(issuerUrl: String?): AuthenticationProvider?
}

@FunctionalInterface
interface TokenValueAuthenticationProvider {
    fun forToken(token: String): AuthenticationProvider?
}

interface CombinedAuthenticationProvider : IssuerAuthenticationProvider, TokenValueAuthenticationProvider

class TrustedIssuerAuthenticationProvider(
    private val securityProperties: JwtSecurityProperties,
    private val sekiClient: SekiClient?
) : CombinedAuthenticationProvider {

    private val logger = KotlinLogging.logger { }

    private val trustedIssuers: Set<String> = securityProperties.issuers.toSet()

    private val managerMap = ConcurrentHashMap<String, AuthenticationProvider>()

    override fun resolve(issuerUrl: String?): AuthenticationProvider? {
        return if (trustedIssuers.contains(issuerUrl) && issuerUrl != null) {
            managerMap.computeIfAbsent(issuerUrl) { _ ->
                val provider = when (issuerUrl) {
                    SEKI_ISSUER_NAME -> {
                        check(sekiClient != null) { "Seki client not configured: cannot validate seki tokens" }

                        if (securityProperties.sekiSharedSecret == null) {
                            // Ideally this branch would only be used for local testing.  It ignores the JWT signature, and because SekiCustomAuthenticationConverter calls seki
                            // to validate, we can consider it like this is an opaque token.  I don't really  like writing this at all, but it could save us the difficulty of
                            // putting secrets in local test environments.  It could also mean, if we're uncomfortable sharing the seki secrets with other apps, we don't need to.
                            JwtAuthenticationProvider(UnsafeJwtDecoder()).apply {
                                setJwtAuthenticationConverter(SekiCustomAuthenticationConverter(sekiClient))
                            }
                        } else {
                            JwtAuthenticationProvider(NimbusJwtDecoder.withSecretKey(SecretKeySpec(securityProperties.sekiSharedSecret.toByteArray(), "HmacSHA256")).build()).apply {
                                setJwtAuthenticationConverter(SekiCustomAuthenticationConverter(sekiClient))
                            }
                        }
                    }

                    else -> JwtAuthenticationProvider(
                        (JwtDecoders.fromIssuerLocation(issuerUrl) as NimbusJwtDecoder).apply {
                            if (securityProperties.validAudiences != null) {
                                setJwtValidator(
                                    DelegatingOAuth2TokenValidator(
                                        listOf(
                                            JwtValidators.createDefaultWithIssuer(issuerUrl),
                                            JwtAudienceValidator(securityProperties.validAudiences)
                                        )
                                    )
                                )
                            } else {
                                logger.warn { "Potential security risk: this service should be configured to verify token audience" }
                            }
                        }
                    ).apply {
                        setJwtAuthenticationConverter(RoninCustomAuthenticationConverter())
                    }
                }
                object : AuthenticationProvider {
                    override fun resolve(authentication: Authentication): Authentication {
                        return provider.authenticate(authentication)
                    }
                }
            }
        } else {
            logger.warn("Request for untrusted issuer $issuerUrl")
            null
        }
    }

    override fun forToken(token: String): AuthenticationProvider? {
        return UnsafeJwtDecoder().decode(token).claims[JwtClaimNames.ISS]?.let { resolve(it.toString()) }
    }
}
