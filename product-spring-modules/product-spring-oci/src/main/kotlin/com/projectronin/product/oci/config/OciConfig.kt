package com.projectronin.product.oci.config

import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider
import com.projectronin.oci.auth.OciAuth
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OciPropertiesImpl::class)
open class OciConfig(val properties: OciPropertiesImpl) {
    @Bean
    open fun authProvider(): SimpleAuthenticationDetailsProvider {
        return OciAuth(properties).provider
    }
}
