package com.projectronin.product.common.config

import com.projectronin.product.common.management.actuator.ThreadDumpTextEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ActuatorConfig {

    @Bean
    open fun threadDumpTextEndpoint() = ThreadDumpTextEndpoint()
}
