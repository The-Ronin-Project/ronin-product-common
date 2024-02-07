package com.projectronin.product.common.tenant.stream.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "ronin.product.tenant")
class TenantStreamProperties(
    val applicationId: String,
    @DefaultValue("oci.us-phoenix-1.ronin-tenant.tenant.v1")
    val tenantTopic: String,
    val dlqTopic: String
)
