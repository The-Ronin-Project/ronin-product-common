package com.projectronin.product.oci.objectstorage.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oci")
class ObjectStorageProperties(
    val compartment: String
)
