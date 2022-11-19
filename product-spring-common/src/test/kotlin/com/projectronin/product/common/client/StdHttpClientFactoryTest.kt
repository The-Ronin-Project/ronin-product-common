package com.projectronin.product.common.client

import com.projectronin.product.common.client.StdHttpClientFactory.CONFIG_KEY_CONNECTION_TIMEOUT
import com.projectronin.product.common.client.StdHttpClientFactory.CONFIG_KEY_READ_TIMEOUT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant

/**
 * Tests for Factory that generates an okHttpCleint for a serviceClient.
 *   used when caller doesn't specify a specific okHttpClient
 */
class StdHttpClientFactoryTest {

    private val EXPECTED_DEFAULT_CONNECTION_TIMEOUT_MILLIS = 30_000
    private val EXPECTED_DEFAULT_READ_TIMEOUT_MILLIS = 30_000

    @Test
    fun `get default client no params`() {
        val defaultClient = StdHttpClientFactory.createClient()
        // expect the created httpClient to match our expected values for connection timeouts
        assertEquals(EXPECTED_DEFAULT_CONNECTION_TIMEOUT_MILLIS, defaultClient.connectTimeoutMillis, "mismatch default connection timeout")
        assertEquals(EXPECTED_DEFAULT_READ_TIMEOUT_MILLIS, defaultClient.readTimeoutMillis, "mismatch default read timeout")
    }

    @Test
    fun `get default client no applicable params`() {
        // pass in a map that doesn't have 'relevant' data will give a default httpClient
        val configMap: Map<String, Any> = mapOf("foo" to 30000, "bar" to "1m")
        val defaultClient = StdHttpClientFactory.createClient(configMap)
        // expect the created httpClient to match our expected values for connection timeouts
        assertEquals(EXPECTED_DEFAULT_CONNECTION_TIMEOUT_MILLIS, defaultClient.connectTimeoutMillis, "mismatch default connection timeout")
        assertEquals(EXPECTED_DEFAULT_READ_TIMEOUT_MILLIS, defaultClient.readTimeoutMillis, "mismatch default read timeout")
    }

    @Test
    fun `get client custom connection timeout`() {
        val customTimeout = 10000
        val configMap: Map<String, Any> = mapOf(CONFIG_KEY_CONNECTION_TIMEOUT to customTimeout)
        val httpClient = StdHttpClientFactory.createClient(configMap)
        assertEquals(customTimeout, httpClient.connectTimeoutMillis, "mismatch client custom connection timeout")
    }

    @Test
    fun `get client read connection timeout`() {
        val customTimeout = 10000
        val configMap: Map<String, Any> = mapOf(CONFIG_KEY_READ_TIMEOUT to customTimeout)
        val httpClient = StdHttpClientFactory.createClient(configMap)
        assertEquals(customTimeout, httpClient.readTimeoutMillis, "mismatch client custom read timeout")
    }

    @ParameterizedTest(name = "Check setting custom connection timeout object: \"{0}\"")
    @MethodSource("getFiveMinuteValues")
    fun `five minute timeout value permutations`(inputValue: Any) {
        val configMap = mapOf(CONFIG_KEY_CONNECTION_TIMEOUT to inputValue)
        val httpClient = StdHttpClientFactory.createClient(configMap)
        assertEquals(300000, httpClient.connectTimeoutMillis, "mismatch client custom object connection timeout")
    }

    @Test()
    fun `invalid timeout object exception`() {
        val configMap = mapOf(CONFIG_KEY_CONNECTION_TIMEOUT to Instant.now())
        val exception = assertThrows<IllegalArgumentException> {
            val httpClient = StdHttpClientFactory.createClient(configMap)
        }
        val expectedSubString = "Unrecognized class"
        assertTrue(exception.message!!.contains(expectedSubString),
            "expected exception message '${exception.message}' to contain substring '${expectedSubString}'")
    }

    companion object {
        // grab a list of various possible configMap values that
        //   all represent "5 minutes" (300,000 milliseconds)
        @JvmStatic
        fun getFiveMinuteValues(): List<Arguments> {
            return listOf(
                Arguments.of(300000),
                Arguments.of(300000L),
                Arguments.of("300s"),
                Arguments.of("5m"),
                Arguments.of("PT5M"),
                Arguments.of(java.time.Duration.ofMillis(300000)),
                Arguments.of(kotlin.time.Duration.parse("5m")),
            )
        }
    }
}
