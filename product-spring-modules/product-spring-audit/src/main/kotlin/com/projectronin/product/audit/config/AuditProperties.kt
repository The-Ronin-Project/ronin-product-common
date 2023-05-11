package com.projectronin.product.audit.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ronin.product.audit")
class AuditProperties(
    val topic: String,
    val sourceService: String
)
