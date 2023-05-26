package com.projectronin.product.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "ronin.product.telemetry")
class TelemetryProperties(
    @DefaultValue("GET /actuator")
    val ignoreTraces: List<String>
)
