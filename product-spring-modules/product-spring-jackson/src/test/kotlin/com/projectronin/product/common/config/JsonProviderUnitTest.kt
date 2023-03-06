package com.projectronin.product.common.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class JsonProviderUnitTest {
    private val dateHolder = DateHolder(
        ZonedDateTime.of(2022, 1, 25, 15, 44, 52, 12345, ZoneOffset.UTC),
        ZonedDateTime.of(2020, 5, 6, 5, 6, 7, 993, ZoneOffset.of("-0600")),
        LocalDateTime.of(2015, 12, 31, 23, 59, 59, 9999),
        LocalDate.of(2022, 2, 1)
    )

    private val expectedJson = """
        {"a":"2022-01-25T15:44:52.000012345Z","b":"2020-05-06T05:06:07.000000993-06:00","c":"2015-12-31T23:59:59.000009999","d":"2022-02-01"}
    """.trimIndent()

    @Test
    fun `serializes datetime to ISO 8601 format`() {
        val json = JsonProvider.objectMapper.writeValueAsString(dateHolder)
        Assertions.assertEquals(expectedJson, json)
    }

    @Test
    fun `deserializes ISO 8601 datetimes`() {
        val deserialized = JsonProvider.objectMapper.readValue<DateHolder>(expectedJson)
        Assertions.assertEquals(dateHolder, deserialized)
    }

    @Test
    fun `ignores extra fields`() {
        val json = """{"a": "123", "b": "456", "c": "789"}"""

        val sampleObject = JsonProvider.objectMapper.readValue<SampleObject>(json)

        Assertions.assertEquals("123", sampleObject.a)
        Assertions.assertEquals("456", sampleObject.b)
    }

    @Test
    fun `ignores missing nullable fields`() {
        val json = """{"a": "123"}"""
        val sampleObject = JsonProvider.objectMapper.readValue<SampleObject>(json)

        Assertions.assertEquals("123", sampleObject.a)
        Assertions.assertEquals(null, sampleObject.b)
    }

    @Test
    fun `errors on missing non-nullable fields`() {
        val json = """{"b": "123"}"""

        assertThrows<MissingKotlinParameterException> {
            JsonProvider.objectMapper.readValue<SampleObject>(json)
        }
    }

    /**
     * Verify the mapper is configured to auto-trim string values
     *   See DASH-3145 for details
     */
    @Test
    fun `verify deserialized string values trimmed`() {
        val json = """{"a": "   123\n\t\n", "b": "  456  "}"""
        val deserialized = JsonProvider.objectMapper.readValue<SampleObject>(json)
        Assertions.assertEquals("123", deserialized.a, "expected trimmed strong on deserialized object")
        Assertions.assertEquals("456", deserialized.b, "expected trimmed strong on deserialized object")
    }

    /**
     * check if special deserializer handles nulls correctly
     *   side note: test motivated by the fact of code coverage report was driving Jacobs bonkers
     */
    @Test
    fun `string space deserializer null edge cases`() {
        val deserializer = StringWithoutSpaceDeserializer(String::class.java)

        val nullMockParser = mockk<JsonParser> { every { text } returns null }
        val emptyMockParser = mockk<JsonParser> { every { text } returns "" }
        val mockContext = mockk<DeserializationContext>()
        Assertions.assertEquals("", deserializer.deserialize(nullMockParser, mockContext))
        Assertions.assertEquals("", deserializer.deserialize(emptyMockParser, mockContext))
    }

    @Test
    fun `string space deserializer doesn't actually produce empties where nulls exist`() {
        val jsonWithNullValue = """{"a": "foo", "b": null}"""

        val obj1 = JsonProvider.objectMapper.readValue<SampleObject>(jsonWithNullValue)
        assertThat(obj1.a).isEqualTo("foo")
        assertThat(obj1.b).isNull()

        val jsonWithNoValue = """{"a": "foo"}"""

        val obj2 = JsonProvider.objectMapper.readValue<SampleObject>(jsonWithNoValue)
        assertThat(obj2.a).isEqualTo("foo")
        assertThat(obj2.b).isNull()
    }
}

private data class DateHolder(
    val a: ZonedDateTime,
    val b: ZonedDateTime,
    val c: LocalDateTime,
    val d: LocalDate
)

private data class SampleObject(val a: String, val b: String?)
