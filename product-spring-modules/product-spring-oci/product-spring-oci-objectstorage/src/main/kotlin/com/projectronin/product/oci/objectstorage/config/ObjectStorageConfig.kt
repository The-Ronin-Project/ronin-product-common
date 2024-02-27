package com.projectronin.product.oci.objectstorage.config

import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest
import com.projectronin.oci.objectstorage.BucketClient
import com.projectronin.oci.objectstorage.RoninOciClient
import com.projectronin.product.oci.config.OciConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Profile("!local")
@Configuration
@EnableConfigurationProperties(ObjectStorageProperties::class)
@Import(OciConfig::class)
open class ObjectStorageConfig @Autowired constructor(
    val properties: ObjectStorageProperties
) {
    @Bean("primaryStorageClient")
    open fun primaryStorageClient(authProvider: AbstractAuthenticationDetailsProvider): RoninOciClient {
        return RoninOciClient(
            properties.primaryRegion,
            ObjectStorageClient.builder()
                .region(properties.primaryRegion)
                .build(authProvider)
        )
    }

    @Bean("secondaryStorageClient")
    @ConditionalOnProperty(prefix = "oci.objectstorage", name = ["secondaryRegion"])
    open fun secondaryStorageClient(authProvider: AbstractAuthenticationDetailsProvider): RoninOciClient {
        return RoninOciClient(
            properties.secondaryRegion!!,
            ObjectStorageClient.builder()
                .region(properties.secondaryRegion)
                .build(authProvider)
        )
    }

    @Bean
    open fun namespace(@Qualifier("primaryStorageClient") primary: RoninOciClient): String {
        return primary.getNamespace(GetNamespaceRequest.builder().build()).value
    }

    @Bean
    open fun bucketClient(
        namespace: String,
        @Qualifier("primaryStorageClient") primary: RoninOciClient,
        @Qualifier("secondaryStorageClient") secondary: RoninOciClient? = null
    ): BucketClient {
        return BucketClient(namespace, properties.compartment, primary, secondary)
    }
}
