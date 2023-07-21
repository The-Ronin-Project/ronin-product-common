package com.projectronin.product.launchdarkly.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "ronin.product.launch-darkly")
class LaunchDarklyProperties(
    val clientSdkKey: String?,
    @DefaultValue("true")
    val diagnosticOptOut: Boolean = true,
    @DefaultValue("false")
    val offline: Boolean = false
)
