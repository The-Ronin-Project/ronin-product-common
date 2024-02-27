package com.projectronin.product.oci.objectstorage.config.health

import com.oracle.bmc.model.BmcException
import com.oracle.bmc.objectstorage.model.BucketSummary
import com.projectronin.oci.objectstorage.BucketClient
import com.projectronin.oci.objectstorage.RoninOciClient
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status
import java.time.Instant

class OciHealthIndicatorTest {
    private val primary = mockk<RoninOciClient>()
    private val secondary = mockk<RoninOciClient>()
    private val namespace = "oci_namespace"
    private val compartment = "compartment.ocid"
    private val exception = BmcException(
        503,
        "ExternalServerUnreachable",
        "A connection with an external system needed to fulfill the request could not be established.",
        "6325ac10-cfea-44fc-bad6-c2fd2a02daf5"
    )

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `health indicator returns up if client retrieves buckets`() {
        val client = BucketClient(namespace, compartment, primary, secondary)
        client.lastHealthCheck = Instant.now().minusSeconds(700)

        every { client.primary.listBuckets(any()).items }.returns(listOf(mockk<BucketSummary>()))
        every { client.secondary?.listBuckets(any())?.items }.returns(listOf(mockk<BucketSummary>()))

        val result = OciHealthIndicator(compartment, client).health()
        assertThat(result.status).isEqualTo(Status.UP)
        assertThat(client.lastHealthException).isEqualTo(null)

        // verify it works if only one client is provided
        val onlyPrimaryClient = BucketClient(namespace, compartment, primary, secondary)

        every { onlyPrimaryClient.primary.listBuckets(any()).items }.returns(listOf(mockk<BucketSummary>()))

        val result2 = OciHealthIndicator(compartment, client).health()
        assertThat(result2.status).isEqualTo(Status.UP)
        assertThat(client.lastHealthException).isEqualTo(null)
    }

    @Test
    fun `health indicator returns down if OCI has an exception`() {
        val client = BucketClient(namespace, compartment, primary)
        client.lastHealthCheck = Instant.now().minusSeconds(700)
        every { client.primary.listBuckets(any()) }.throws(exception)
        val result = OciHealthIndicator(compartment, client).health()
        assertThat(result.status).isEqualTo(Status.DOWN)
        assertThat(result.details).isEqualTo(mapOf("error" to exception.toString()))
        assertThat(client.lastHealthException).isEqualTo(exception)
    }

    @Test
    fun `health checks only run every 5 minutes`() {
        val client = BucketClient(namespace, compartment, primary)
        client.lastHealthCheck = Instant.now().minusSeconds(5)

        val upResult = OciHealthIndicator(compartment, client).health()
        verify(exactly = 0) { client.primary.listBuckets(any()) }
        assertThat(upResult.status).isEqualTo(Status.UP)

        client.lastHealthException = exception
        val downResult = OciHealthIndicator(compartment, client).health()
        verify(exactly = 0) { client.primary.listBuckets(any()) }
        assertThat(downResult.status).isEqualTo(Status.DOWN)
    }
}
