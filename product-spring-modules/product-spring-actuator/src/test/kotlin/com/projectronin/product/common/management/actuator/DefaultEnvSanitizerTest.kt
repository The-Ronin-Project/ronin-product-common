package com.projectronin.product.common.management.actuator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.endpoint.SanitizableData

class DefaultEnvSanitizerTest {

    companion object {
        private const val EXPECTED_SANITIZE_STRING = "******"
    }

    // sanitize should be a 'no-op' for basic config values
    @Test
    fun `sanitize safe value`() {
        val testValue = "foobar"
        val inputData = SanitizableData(null, "management.something.name", testValue)
        val sanitizedData = DefaultEnvSanitizer().apply(inputData)
        assertEquals(testValue, sanitizedData?.value.toString(), "expected value to remain unchanged")
    }

    // keys detected as a 'password' should be sanitized
    @Test
    fun `sanitize password value`() {
        val testValue = "myPassword1234"
        val inputData = SanitizableData(null, "db.user.password", testValue)
        val sanitizedData = DefaultEnvSanitizer().apply(inputData)
        assertEquals(EXPECTED_SANITIZE_STRING, sanitizedData?.value.toString(), "expected value to be sanitized")
    }

    // keys detected as 'credentials' should be sanitized (uses a regex match)
    @Test
    fun `sanitize credentials value`() {
        val testValue = "myToken1234"
        val inputData = SanitizableData(null, "db.credentials.password", testValue)
        val sanitizedData = DefaultEnvSanitizer().apply(inputData)
        assertEquals(EXPECTED_SANITIZE_STRING, sanitizedData?.value.toString(), "expected value to be sanitized")
    }

    @Test
    fun `test null case`() {
        // null in expected to return null out
        val sanitizedData = DefaultEnvSanitizer().apply(null)
        assertNull(sanitizedData, "expected sanitizedData object to be null")
    }
}
