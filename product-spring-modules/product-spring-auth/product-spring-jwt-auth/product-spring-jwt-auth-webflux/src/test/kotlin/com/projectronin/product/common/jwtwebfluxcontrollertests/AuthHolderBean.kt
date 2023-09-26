package com.projectronin.product.common.jwtwebfluxcontrollertests

import com.projectronin.auth.RoninAuthentication
import org.springframework.stereotype.Service

@Service
class AuthHolderBean {

    var latestRoninAuth: RoninAuthentication? = null

    fun reset() {
        latestRoninAuth = null
    }
}
