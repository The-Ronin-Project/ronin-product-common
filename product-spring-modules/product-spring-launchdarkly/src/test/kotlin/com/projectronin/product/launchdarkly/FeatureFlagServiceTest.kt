package com.projectronin.product.launchdarkly

import com.launchdarkly.sdk.LDContext
import com.launchdarkly.sdk.LDValue
import com.projectronin.product.launchdarkly.config.LaunchDarklyConfig
import com.projectronin.product.launchdarkly.config.LaunchDarklyProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FeatureFlagServiceTest {
    @Test
    fun testOfflineMode() {
        val config = LaunchDarklyConfig()
        val props = LaunchDarklyProperties("test-key", offline = true)
        val client = config.launchDarklyClient(props)
        val flagKey = "some-flag"
        val context = LDContext.builder("testUser").build()

        assertTrue(props.diagnosticOptOut)
        assertFalse(client.boolVariation(flagKey, context, false))

        client.close()
    }

    @Test
    fun testTestDataClient() {
        val config = LaunchDarklyConfig()
        val testData = config.testData()
        val client = config.testLaunchDarklyClient(testData)
        val service = LaunchDarklyFeatureFlagService(client)

        val boolFlag = "bool-flag"
        val stringFlag = "string-flag"
        val intFlag = "int-flag"

        val context1 = "user1"
        val context2 = "user2"

        testData.run {
            update(flag(boolFlag).booleanFlag().variationForUser(context1, true).fallthroughVariation(false))
            update(flag(intFlag).variations(LDValue.of(0), LDValue.of(1)).variationForUser(context1, 1).fallthroughVariation(0))
            update(flag(stringFlag).variations(LDValue.of("goodbye"), LDValue.of("hello")).variationForUser(context1, 1).fallthroughVariation(0))
        }

        assertTrue(service.flag(boolFlag, context1, false))
        assertFalse(service.flag(boolFlag, context2, false))
        assertEquals(1, service.flag(intFlag, context1, 0))
        assertEquals(0, service.flag(intFlag, context2, 0))
        assertEquals("hello", service.flag(stringFlag, context1, "default"))
        assertEquals("goodbye", service.flag(stringFlag, context2, "default"))

        client.close()
    }
}
