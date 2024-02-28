package com.projectronin.product.oci.config

import com.projectronin.oci.OciProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "oci", ignoreInvalidFields = true, ignoreUnknownFields = true)
data class OciPropertiesImpl @ConstructorBinding constructor(
    override val tenant: String,
    override val user: String,
    override val fingerprint: String,
    override val privateKey: String,
    override val primaryRegion: String,
    override val secondaryRegion: String? = null
) : OciProperties
