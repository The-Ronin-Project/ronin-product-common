package com.projectronin.product.common.config

import com.projectronin.product.common.management.actuator.ThreadDumpTextEndpoint
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(prefix = "ronin.product", name = ["actuator"], matchIfMissing = true)
open class ActuatorConfig {

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product.actuator", name = ["thread-dump"], matchIfMissing = true)
    open fun threadDumpTextEndpoint() = ThreadDumpTextEndpoint()
}
