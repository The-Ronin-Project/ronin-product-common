package com.projectronin.product.common.auth.token

class RoninUser(
    val id: String,
    val userType: RoninUserType,
    val name: RoninName?,
    val preferredTimeZone: String?,
    val loginProfile: RoninLoginProfile?,
    val identities: List<RoninUserIdentity>,
    val authenticationSchemes: List<RoninAuthenticationScheme>
)
