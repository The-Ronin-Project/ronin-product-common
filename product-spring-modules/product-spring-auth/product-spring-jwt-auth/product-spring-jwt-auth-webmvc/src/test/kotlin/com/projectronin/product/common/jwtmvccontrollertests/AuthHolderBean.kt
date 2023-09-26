package com.projectronin.product.common.jwtmvccontrollertests

import com.projectronin.auth.RoninAuthentication
import org.springframework.stereotype.Service

@Service
class AuthHolderBean {

    var latestRoninAuth: RoninAuthentication? = null

    fun reset() {
        latestRoninAuth = null
    }
}
