package com.projectronin.product.oci.config

import com.projectronin.oci.OciProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@EnableConfigurationProperties(value = [OciPropertiesImpl::class])
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
class OciPropertiesImplTest(@Autowired val properties: OciProperties) {

    @Test
    fun `test OCI Properties`() {
        assertThat(properties.tenant).isEqualTo("oci-tenant-ocid")
        assertThat(properties.user).isEqualTo("user")
        assertThat(properties.fingerprint).isEqualTo("fingerprint")
        assertThat(properties.privateKey).isEqualTo("pvtkeys")
        assertThat(properties.primaryRegion).isEqualTo("us-phoenix-1")
        assertThat(properties.secondaryRegion).isEqualTo("us-ashburn-1")
    }
}
