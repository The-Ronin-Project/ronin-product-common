package com.projectronin.product.oci.objectstorage.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@EnableConfigurationProperties(value = [ObjectStorageProperties::class])
@TestPropertySource(
    properties = [
        "oci.tenant = oci-tenant-ocid",
        "oci.user = user",
        "oci.fingerprint = fingerprint",
        "oci.privateKey = pvtkeys",
        "oci.primaryRegion = us-phoenix-1",
        "oci.secondaryRegion = us-ashburn-1",
        "oci.compartment = compartment"
    ]
)
class ObjectStoragePropertiesTest(@Autowired val properties: ObjectStorageProperties) {

    @Test
    fun `test Object Storage Properties`() {
        assertThat(properties.compartment).isEqualTo("compartment")
    }
}
