package com.projectronin.product.common.config

import com.projectronin.product.common.base.ModulePropertySourceFactory
import com.projectronin.product.telemetry.IgnoreInterceptor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.PropertySource

@AutoConfiguration
@ConditionalOnProperty(name = ["ronin.product.telemetry"], matchIfMissing = true)
@EnableConfigurationProperties(TelemetryProperties::class)
@PropertySource(
    name = "Telemetry Configuration",
    value = ["classpath:telemetry-application.yml"],
    factory = ModulePropertySourceFactory::class
)
open class TelemetryConfig(telemetryProperties: TelemetryProperties) {
    init {
        IgnoreInterceptor.create(telemetryProperties.ignoreTraces)
    }
}
