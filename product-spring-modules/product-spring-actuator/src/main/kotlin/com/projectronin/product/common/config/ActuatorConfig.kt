package com.projectronin.product.common.config

import com.projectronin.product.common.base.ModulePropertySourceFactory
import com.projectronin.product.common.management.actuator.DefaultEnvSanitizer
import com.projectronin.product.common.management.actuator.ThreadDumpTextEndpoint
import org.springframework.boot.actuate.endpoint.SanitizingFunction
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource

@AutoConfiguration
@ConditionalOnProperty(name = ["ronin.product.actuator"], matchIfMissing = true)
@PropertySource(
    name = "Actuator Configuration",
    value = ["classpath:actuator-application.yml"],
    factory = ModulePropertySourceFactory::class
)
open class ActuatorConfig {

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product.actuator", name = ["thread-dump"], matchIfMissing = true)
    open fun threadDumpTextEndpoint() = ThreadDumpTextEndpoint()

    @Bean
    open fun getSanitizingFunction(): SanitizingFunction = DefaultEnvSanitizer()
}
