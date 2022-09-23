package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.model.User
import org.springframework.security.core.Authentication

class RoninAuthentication(delegateAuthentication: Authentication, private val sekiUser: User) : Authentication by delegateAuthentication {

    val tenantId: String
        get() = sekiUser.tenantId

    val userId: String
        get() = sekiUser.id

    val userFirstName: String
        get() = sekiUser.firstName

    val userLastName: String
        get() = sekiUser.lastName

    val userFullName: String
        get() = sekiUser.fullName
}
