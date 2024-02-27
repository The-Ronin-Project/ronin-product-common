package com.projectronin.product.oci.objectstorage.config.health

import com.oracle.bmc.model.BmcException
import com.oracle.bmc.objectstorage.requests.ListBucketsRequest
import com.projectronin.oci.objectstorage.BucketClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import java.time.Instant

class OciHealthIndicator(
    private val compartment: String,
    private val bucketClient: BucketClient
) : HealthIndicator {
    override fun health(): Health {
        // Only check every 5 minutes
        if (bucketClient.lastHealthCheck.isBefore(Instant.now().minusSeconds(300))) {
            bucketClient.lastHealthCheck = Instant.now()
            try {
                val request = ListBucketsRequest.builder()
                    .namespaceName(bucketClient.namespace)
                    .compartmentId(compartment)
                    .build()

                bucketClient.primary.listBuckets(request)
                bucketClient.secondary?.listBuckets(request)
            } catch (e: BmcException) {
                bucketClient.lastHealthException = e
                return Health.down(e).build()
            }

            bucketClient.lastHealthException = null
            return Health.up().build()
        } else {
            return when (bucketClient.lastHealthException == null) {
                true -> Health.up().build()
                false -> Health.down(bucketClient.lastHealthException).build()
            }
        }
    }
}
