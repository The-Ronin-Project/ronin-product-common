package com.projectronin.product.common.auth

import com.projectronin.auth.RoninAuthentication
import com.projectronin.auth.RoninClaimsAuthentication
import com.projectronin.auth.RoninClaimsAuthentication.Companion.roninClaimsKey
import com.projectronin.auth.token.RoninClaims
import com.projectronin.product.common.config.JsonProvider
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class RoninJwtAuthenticationToken(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>?,
    name: String?
) : JwtAuthenticationToken(jwt, authorities, name), RoninClaimsAuthentication, RoninAuthentication {

    constructor(jwt: Jwt) : this(jwt, null, null)

    constructor(jwt: Jwt, authorities: Collection<GrantedAuthority>) : this(jwt, authorities, null)

    override val objectMapper = JsonProvider.objectMapper

    override val roninClaimMap: Map<String, Any>?
        get() = token.getClaim(roninClaimsKey) as Map<String, Any>?

    override val roninClaims: RoninClaims by lazy {
        decodeRoninClaimsFromMap()
    }

    override val tokenValue: String by lazy {
        jwt.tokenValue
    }
}
