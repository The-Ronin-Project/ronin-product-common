package com.projectronin.product.common.config

import com.projectronin.product.telemetry.IgnoreInterceptor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties

@AutoConfiguration
@ConditionalOnProperty(name = ["ronin.product.telemetry"], matchIfMissing = true)
@EnableConfigurationProperties(TelemetryProperties::class)
open class TelemetryConfig(telemetryProperties: TelemetryProperties) {
    init {
        IgnoreInterceptor.create(telemetryProperties.ignoreTraces)
    }
}
