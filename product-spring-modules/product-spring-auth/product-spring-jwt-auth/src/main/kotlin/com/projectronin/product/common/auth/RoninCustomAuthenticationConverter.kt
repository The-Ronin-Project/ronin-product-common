package com.projectronin.product.common.auth

import com.projectronin.product.common.config.SEKI_ISSUER_NAME
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

internal class RoninCustomAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    private val authorityConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        if (jwt.getClaimAsString(JwtClaimNames.ISS) == SEKI_ISSUER_NAME) {
            throw BadCredentialsException("Invalid configuration or Seki not configured.  RoninCustomAuthenticationConverter cannot handle seki tokens.")
        }
        return RoninJwtAuthenticationToken(jwt, authorityConverter.convert(jwt) ?: emptyList()).apply {
            isAuthenticated = true
        }
    }
}
