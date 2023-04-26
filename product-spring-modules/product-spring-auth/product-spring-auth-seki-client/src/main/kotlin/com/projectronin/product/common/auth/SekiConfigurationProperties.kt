package com.projectronin.product.common.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

const val SEKI_PROPERTIES_PREFIX = "seki"

@ConfigurationProperties(SEKI_PROPERTIES_PREFIX)
data class SekiConfigurationProperties @ConstructorBinding constructor(
    val url: String?
)
