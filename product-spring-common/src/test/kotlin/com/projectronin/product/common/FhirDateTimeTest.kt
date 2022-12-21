package com.projectronin.product.common

import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.product.common.config.JsonProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

private const val FHIR_DATE_TIME = "\"2022-12-16T17:22:06.987Z\""
private const val FHIR_DATE = "\"2022-12-16\""
private const val FHIR_MONTH = "\"2022-12\""
private const val FHIR_YEAR = "\"2022\""

class FhirDateTimeTest {

    private val objectMapper = JsonProvider.objectMapper

    @Test
    fun `FhirDateTime deserializes`() {
        val json = "\"2022-12-16T17:22:06.987Z\""
        objectMapper.readValue<FhirDateTime>(json)
    }

    @Test
    fun `FhirDateTime serializes`() {
        val json = "\"2022-12-16T17:22:06.987Z\""
        val output = objectMapper.writeValueAsString(FhirDateTime("2022-12-16T17:22:06.987Z"))
        assertEquals(json, output)
    }

    @Test
    fun `getParsed OffsetDateTime`() {
        val parsed: FhirDateTime.FhirDateTimeValue = objectMapper.readValue<FhirDateTime>(FHIR_DATE_TIME).parsed
        assertInstanceOf(FhirDateTime.DateTimeValue::class.java, parsed)
    }

    @Test
    fun `getParsed LocalDate`() {
        val parsed: FhirDateTime.FhirDateTimeValue = objectMapper.readValue<FhirDateTime>(FHIR_DATE).parsed
        assertInstanceOf(FhirDateTime.DateValue::class.java, parsed)
    }

    @Test
    fun `getParsed YearMonth`() {
        val parsed: FhirDateTime.FhirDateTimeValue = objectMapper.readValue<FhirDateTime>(FHIR_MONTH).parsed
        assertInstanceOf(FhirDateTime.YearMonthValue::class.java, parsed)
    }

    @Test
    fun `getParsed Year`() {
        val parsed: FhirDateTime.FhirDateTimeValue = objectMapper.readValue<FhirDateTime>(FHIR_YEAR).parsed
        assertInstanceOf(FhirDateTime.YearValue::class.java, parsed)
    }

    @Test
    fun `getParsed Unknown`() {
        val parsed: FhirDateTime.FhirDateTimeValue = objectMapper.readValue<FhirDateTime>("\"A random string that isn't an ISO datetime\"").parsed
        assertInstanceOf(FhirDateTime.Unknown::class.java, parsed)
    }
}
