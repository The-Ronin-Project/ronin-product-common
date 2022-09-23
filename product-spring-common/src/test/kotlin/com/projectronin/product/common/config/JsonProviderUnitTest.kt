package com.projectronin.product.common.config

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.product.common.config.JsonProvider.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

private data class DateHolder(
    val a: ZonedDateTime,
    val b: ZonedDateTime,
    val c: LocalDateTime,
    val d: LocalDate
)

private data class SampleObject(val a: String, val b: String?)

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
        val json = objectMapper.writeValueAsString(dateHolder)
        assertEquals(expectedJson, json)
    }

    @Test
    fun `deserializes ISO 8601 datetimes`() {
        val deserialized = objectMapper.readValue<DateHolder>(expectedJson)
        assertEquals(dateHolder, deserialized)
    }

    @Test
    fun `ignores extra fields`() {
        val json = """{"a": "123", "b": "456", "c": "789"}"""

        val sampleObject = objectMapper.readValue<SampleObject>(json)

        assertEquals("123", sampleObject.a)
        assertEquals("456", sampleObject.b)
    }

    @Test
    fun `ignores missing nullable fields`() {
        val json = """{"a": "123"}"""
        val sampleObject = objectMapper.readValue<SampleObject>(json)

        assertEquals("123", sampleObject.a)
        assertEquals(null, sampleObject.b)
    }

    @Test
    fun `errors on missing non-nullable fields`() {
        val json = """{"b": "123"}"""

        assertThrows<MissingKotlinParameterException> {
            objectMapper.readValue<SampleObject>(json)
        }
    }
}
