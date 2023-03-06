package com.projectronin.product.common.management.actuator

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ThreadDumpTextEndpointTest {

    @Test
    fun `get thread dump string`() {
        // simple test for thread dump ss string.
        //   (and don't want codecoverage saying this class is 0% coverage)
        assertTrue(ThreadDumpTextEndpoint().textThreadDump().contains("Thread"), "invalid threaddummp string")
    }
}
