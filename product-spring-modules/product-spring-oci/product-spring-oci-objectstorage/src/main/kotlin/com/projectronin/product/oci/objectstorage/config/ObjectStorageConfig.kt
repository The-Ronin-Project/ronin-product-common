package com.projectronin.product.oci.objectstorage.config

import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider
import com.oracle.bmc.http.client.Options
import com.projectronin.bucketstorage.BucketStorage
import com.projectronin.oci.OciProperties
import com.projectronin.oci.objectstorage.BucketClient
import com.projectronin.oci.objectstorage.ObjectStoreBucketStorage
import com.projectronin.oci.objectstorage.OciObjectStorageClient
import com.projectronin.product.oci.config.OciConfig
import com.projectronin.product.oci.objectstorage.config.health.OciHealthIndicator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ConditionalOnProperty(prefix = "oci.objectstorage", name = ["enabled"], matchIfMissing = true)
@Import(OciConfig::class)
open class ObjectStorageConfig @Autowired constructor(
    private val properties: OciProperties,
    @Value("\${oci.compartment}") private val compartment: String
) {
    @Bean("primaryStorageClient")
    open fun primaryStorageClient(authProvider: AbstractAuthenticationDetailsProvider): OciObjectStorageClient {
        return OciObjectStorageClient(properties.primaryRegion, authProvider)
    }

    @Bean("secondaryStorageClient")
    @ConditionalOnProperty(prefix = "oci.objectstorage", name = ["secondaryRegion"])
    open fun secondaryStorageClient(authProvider: AbstractAuthenticationDetailsProvider): OciObjectStorageClient {
        return OciObjectStorageClient(properties.secondaryRegion!!, authProvider)
    }

    @Bean
    open fun namespace(@Qualifier("primaryStorageClient") primary: OciObjectStorageClient): String {
        return primary.getNamespace()
    }

    @Bean
    open fun bucketClient(
        namespace: String,
        @Qualifier("primaryStorageClient") primary: OciObjectStorageClient,
        @Qualifier("secondaryStorageClient") secondary: OciObjectStorageClient? = null
    ): BucketClient {
        return BucketClient(namespace, compartment, primary, secondary)
    }

    @Bean
    open fun ociHealthIndicator(bucketClient: BucketClient): HealthIndicator {
        return OciHealthIndicator(compartment, bucketClient)
    }

    @Bean
    open fun bucketStorage(bucketClient: BucketClient): BucketStorage {
        Options.shouldAutoCloseResponseInputStream(false)
        return ObjectStoreBucketStorage(bucketClient)
    }

}
