package com.projectronin.product.common.auth

import org.springframework.security.core.Authentication

/**
 * A base extension of the Authentication abstraction.  Makes no assumptions about
 * where the authentication came from.
 */
interface RoninAuthentication : Authentication {

    val tenantId: String

    val userId: String

    val udpId: String?

    val userFirstName: String

    val userLastName: String

    val userFullName: String
}
