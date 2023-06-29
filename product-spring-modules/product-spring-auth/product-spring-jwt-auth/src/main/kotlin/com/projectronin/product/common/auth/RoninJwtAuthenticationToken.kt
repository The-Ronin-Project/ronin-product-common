package com.projectronin.product.common.auth

import com.fasterxml.jackson.module.kotlin.convertValue
import com.projectronin.product.common.auth.token.RoninClaims
import com.projectronin.product.common.auth.token.RoninUserType
import com.projectronin.product.common.config.JsonProvider
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class RoninJwtAuthenticationToken(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>?,
    name: String?
) : JwtAuthenticationToken(jwt, authorities, name), RoninAuthentication {

    companion object {
        const val roninClaimsKey = "urn:projectronin:authorization:claims:version:1"
    }

    constructor(jwt: Jwt) : this(jwt, null, null)

    constructor(jwt: Jwt, authorities: Collection<GrantedAuthority>) : this(jwt, authorities, null)

    override val tenantId: String
        get() = roninClaims.user?.loginProfile?.accessingTenantId ?: ""

    override val userId: String
        get() = roninClaims.user?.id ?: ""

    override val udpId: String?
        get() = roninClaims.user?.loginProfile?.let { profile ->
            when (roninClaims.user?.userType) {
                RoninUserType.Provider -> profile.accessingProviderUdpId
                RoninUserType.Patient -> profile.accessingPatientUdpId
                else -> null
            }
        }

    override val providerRoninId: String?
        get() = roninClaims.user?.loginProfile?.accessingProviderUdpId

    override val patientRoninId: String?
        get() = roninClaims.user?.loginProfile?.accessingPatientUdpId

    override val userFirstName: String
        get() = roninClaims.user?.name?.givenName?.firstOrNull() ?: ""

    override val userLastName: String
        get() = roninClaims.user?.name?.familyName ?: ""

    override val userFullName: String
        get() = roninClaims.user?.name?.fullText ?: ""

    override val roninClaims: RoninClaims by lazy {
        when (val claim = jwt.getClaim(roninClaimsKey) as Map<String, Any>?) {
            null -> RoninClaims(null)
            else -> JsonProvider.objectMapper.convertValue(claim)
        }
    }

    override val tokenValue: String by lazy {
        jwt.tokenValue
    }
}
