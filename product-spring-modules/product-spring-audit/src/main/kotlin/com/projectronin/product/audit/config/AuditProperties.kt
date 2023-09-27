package com.projectronin.product.audit.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "ronin.product.audit")
class AuditProperties(
    @DefaultValue("oci.us-phoenix-1.ronin-audit.command.v1")
    val topic: String,
    val sourceService: String,
    @DefaultValue("true")
    val enabled: Boolean = true
)
