package com.projectronin.product.oci.objectstorage.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oci", ignoreInvalidFields = true, ignoreUnknownFields = true)
class ObjectStorageProperties(
    val compartment: String
)
