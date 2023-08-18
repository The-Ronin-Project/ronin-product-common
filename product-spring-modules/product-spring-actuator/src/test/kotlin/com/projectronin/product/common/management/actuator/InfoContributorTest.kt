package com.projectronin.product.common.management.actuator

import com.projectronin.product.common.config.ActuatorConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.info.Info

class InfoContributorTest {

    @Test
    fun `produces a valid info builder`() {
        val builder = Info.Builder()
        ActuatorConfig().getInfoContributor().contribute(builder)
        val info = builder.build()
        println(info)
    }
}
