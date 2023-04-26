package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.exception.SekiInvalidTokenException
import com.projectronin.product.common.config.SEKI_ISSUER_NAME
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames

internal class SekiCustomAuthenticationConverter(val sekiClient: SekiClient) : Converter<Jwt, AbstractAuthenticationToken> {
    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        if (jwt.getClaimAsString(JwtClaimNames.ISS) != SEKI_ISSUER_NAME) {
            throw BadCredentialsException("Invalid configuration.  SekiCustomAuthenticationConverter only handles Seki tokens.")
        }

        try {
            val authResponse = sekiClient.validate(jwt.tokenValue)
            val token = SekiJwtAuthenticationToken(jwt, authResponse.user, authResponse.userSession)
            token.isAuthenticated = true
            return token
        } catch (e: Exception) {
            // for any exception, convert it to a AuthenticationException to adhere to
            //  the original SpringBoot 'authenticate' method signature we are overriding
            when (e) {
                is SekiInvalidTokenException -> throw BadCredentialsException("Invalid Seki Token", e)
                else -> throw BadCredentialsException("Unable to verify seki token: ${e.message}", e)
            }
        }
    }
}
