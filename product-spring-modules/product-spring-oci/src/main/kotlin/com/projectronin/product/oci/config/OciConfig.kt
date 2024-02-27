package com.projectronin.product.oci.config

import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider
import com.projectronin.oci.auth.OciAuth
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!local")
@Configuration
@EnableConfigurationProperties(OciPropertiesImpl::class)
open class OciConfig @Autowired constructor(
    val properties: OciPropertiesImpl
) {

    @Bean
    open fun authProvider(): SimpleAuthenticationDetailsProvider {
        return OciAuth(properties).provider
    }
}
