package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import org.springframework.security.core.Authentication

/**
 * Seki based [Authentication] which includes details from the seki [User]
 *
 * @param delegateAuthentication core Spring authentication to delegate to
 * @property sekiUser the user data from seki
 * @property sekiSession the session data from seki
 */
class SekiRoninAuthentication(
    delegateAuthentication: Authentication,
    val sekiUser: User,
    val sekiSession: UserSession
) : RoninAuthentication, Authentication by delegateAuthentication {

    override val tenantId: String
        get() = sekiUser.tenantId

    override val userId: String
        get() = sekiUser.id

    override val udpId: String?
        get() = sekiUser.udpId

    override val userFirstName: String
        get() = sekiUser.firstName

    override val userLastName: String
        get() = sekiUser.lastName

    override val userFullName: String
        get() = sekiUser.fullName
}
