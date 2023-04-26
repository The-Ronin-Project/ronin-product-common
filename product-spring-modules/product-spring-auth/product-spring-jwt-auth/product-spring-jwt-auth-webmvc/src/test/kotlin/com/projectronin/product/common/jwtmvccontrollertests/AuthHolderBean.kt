package com.projectronin.product.common.jwtmvccontrollertests

import com.projectronin.product.common.auth.RoninAuthentication
import org.springframework.stereotype.Service

@Service
class AuthHolderBean {

    var latestRoninAuth: RoninAuthentication? = null

    fun reset() {
        latestRoninAuth = null
    }
}
