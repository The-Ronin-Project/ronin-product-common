package com.projectronin.product.oci.objectstorage.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oci.objectstorage")
class ObjectStorageProperties(
    val primaryRegion: String,
    val secondaryRegion: String?,
    val compartment: String
)
