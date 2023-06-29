package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import com.projectronin.product.common.auth.token.RoninClaims
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class SekiJwtAuthenticationToken(
    jwt: Jwt,
    val sekiUser: User,
    val sekiSession: UserSession,
    authorities: Collection<GrantedAuthority>?,
    name: String?
) : JwtAuthenticationToken(jwt, authorities, name), RoninAuthentication {

    constructor(jwt: Jwt, sekiUser: User, sekiSession: UserSession) : this(jwt, sekiUser, sekiSession, null, null)

    constructor(jwt: Jwt, sekiUser: User, sekiSession: UserSession, authorities: Collection<GrantedAuthority>) : this(jwt, sekiUser, sekiSession, authorities, null)

    override val tenantId: String
        get() = sekiUser.tenantId

    override val userId: String
        get() = sekiUser.id

    override val udpId: String?
        get() = sekiUser.udpId

    override val providerRoninId: String?
        get() = roninClaims.user?.loginProfile?.accessingProviderUdpId

    override val patientRoninId: String?
        get() = roninClaims.user?.loginProfile?.accessingPatientUdpId

    override val userFirstName: String
        get() = sekiUser.firstName

    override val userLastName: String
        get() = sekiUser.lastName

    override val userFullName: String
        get() = sekiUser.fullName

    override val roninClaims: RoninClaims by lazy {
        SekiDataToRoninClaimsConverter(sekiUser, sekiSession).roninClaims
    }

    override val tokenValue: String by lazy {
        jwt.tokenValue
    }
}
