package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.model.User
import org.springframework.security.core.Authentication

/**
 * Seki based [Authentication] which includes details from the seki [User]
 *
 * @param delegateAuthentication core Spring authentication to delegate to
 * @param sekiUser the user data from seki
 */
class RoninAuthentication(delegateAuthentication: Authentication, private val sekiUser: User) : Authentication by delegateAuthentication {

    val tenantId: String
        get() = sekiUser.tenantId

    val userId: String
        get() = sekiUser.id

    val udpId: String?
        get() = sekiUser.udpId

    val userFirstName: String
        get() = sekiUser.firstName

    val userLastName: String
        get() = sekiUser.lastName

    val userFullName: String
        get() = sekiUser.fullName
}
