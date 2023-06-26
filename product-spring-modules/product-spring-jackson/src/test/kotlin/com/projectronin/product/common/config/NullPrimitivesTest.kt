package com.projectronin.product.common.config

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NullPrimitivesTest {

    class NonNullInt(val number: Int)
    class NullableInt(val number: Int?)

    class NonNullString(val text: String)
    class NullableString(val text: String?)

    class NonNullBoolean(val bool: Boolean)
    class NullableBoolean(val bool: Boolean?)

    @Test
    fun `unexpectedly null int results in an error`() {
        @Language("JSON")
        val presentJson = """{ "number": 12 }"""

        @Language("JSON")
        val nullJson = """{ "number": null }"""

        // Initial test to confirm if there is a problem, it's with the null value
        val present: NonNullInt = JsonProvider.objectMapper.readValue(presentJson)
        assertEquals(12, present.number)

        assertThrows<MismatchedInputException> {
            JsonProvider.objectMapper.readValue<NonNullInt>(nullJson)
        }
    }

    @Test
    fun `unexpectedly missing int results in an error`() {
        @Language("JSON")
        val missingJson = """{ }"""

        assertThrows<MismatchedInputException> {
            JsonProvider.objectMapper.readValue<NonNullInt>(missingJson)
        }
    }

    @Test
    fun `expected null int results in no error`() {
        @Language("JSON")
        val presentJson = """{ "number": 12 }"""

        @Language("JSON")
        val nullJson = """{ "number": null }"""

        // Initial test to confirm if there is a problem, it's with the null value
        val present: NullableInt = JsonProvider.objectMapper.readValue(presentJson)
        assertEquals(12, present.number)

        val nullable: NullableInt = JsonProvider.objectMapper.readValue(nullJson)
        assertNull(nullable.number)
    }

    @Test
    fun `unexpected missing nullable int results in a null`() {
        @Language("JSON")
        val missingJson = """{ }"""

        val missing: NullableInt = JsonProvider.objectMapper.readValue(missingJson)
        assertNull(missing.number)
    }

    @Test
    fun `unexpectedly null string results in an error`() {
        @Language("JSON")
        val presentJson = """{ "text": "foo bar" }"""

        @Language("JSON")
        val nullJson = """{ "text": null }"""

        // Initial test to confirm if there is a problem, it's with the null value
        val present: NonNullString = JsonProvider.objectMapper.readValue(presentJson)
        assertEquals("foo bar", present.text)

        assertThrows<MismatchedInputException> {
            JsonProvider.objectMapper.readValue<NonNullString>(nullJson)
        }
    }

    @Test
    fun `unexpectedly missing string results in an error`() {
        @Language("JSON")
        val missingJson = """{ }"""

        assertThrows<MismatchedInputException> {
            JsonProvider.objectMapper.readValue<NonNullString>(missingJson)
        }
    }

    @Test
    fun `expected null string results in no error`() {
        @Language("JSON")
        val presentJson = """{ "text": "foo bar" }"""

        @Language("JSON")
        val nullJson = """{ "text": null }"""

        // Initial test to confirm if there is a problem, it's with the null value
        val present: NullableString = JsonProvider.objectMapper.readValue(presentJson)
        assertEquals("foo bar", present.text)

        val nullable: NullableString = JsonProvider.objectMapper.readValue(nullJson)
        assertNull(nullable.text)
    }

    @Test
    fun `unexpected missing nullable string results in a null`() {
        @Language("JSON")
        val missingJson = """{ }"""

        val missing: NullableString = JsonProvider.objectMapper.readValue(missingJson)
        assertNull(missing.text)
    }

    @Test
    fun `unexpected null boolean results in an error`() {
        @Language("JSON")
        val trueJson = """{ "bool": true }"""

        @Language("JSON")
        val falseJson = """{ "bool": false }"""

        @Language("JSON")
        val nullJson = """{ "bool": null }"""

        // Initial test to confirm if there is a problem, it's with the null value
        val trueable: NonNullBoolean = JsonProvider.objectMapper.readValue(trueJson)
        assertTrue(trueable.bool)

        val falseable: NonNullBoolean = JsonProvider.objectMapper.readValue(falseJson)
        assertFalse(falseable.bool)

        assertThrows<MismatchedInputException> {
            JsonProvider.objectMapper.readValue<NonNullBoolean>(nullJson)
        }
    }

    @Test
    fun `unexpected missing boolean results in an error`() {
        @Language("JSON")
        val missingJson = """{ }"""

        assertThrows<MismatchedInputException> {
            JsonProvider.objectMapper.readValue<NonNullBoolean>(missingJson)
        }
    }

    @Test
    fun `expected null boolean results in no error`() {
        @Language("JSON")
        val trueJson = """{ "bool": true }"""

        @Language("JSON")
        val falseJson = """{ "bool": false }"""

        @Language("JSON")
        val nullJson = """{ "bool": null }"""

        // Initial test to confirm if there is a problem, it's with the null value
        val trueable: NullableBoolean = JsonProvider.objectMapper.readValue(trueJson)
        assertNotNull(trueable.bool)
        assertTrue(trueable.bool!!)

        val falseable: NullableBoolean = JsonProvider.objectMapper.readValue(falseJson)
        assertNotNull(falseable.bool)
        assertFalse(falseable.bool!!)

        val nullable: NullableBoolean = JsonProvider.objectMapper.readValue(nullJson)
        assertNull(nullable.bool)
    }

    @Test
    fun `unexpected missing nullable boolean results in a null`() {
        @Language("JSON")
        val missingJson = """{ }"""

        val missing: NullableBoolean = JsonProvider.objectMapper.readValue(missingJson)
        assertNull(missing.bool)
    }
}
