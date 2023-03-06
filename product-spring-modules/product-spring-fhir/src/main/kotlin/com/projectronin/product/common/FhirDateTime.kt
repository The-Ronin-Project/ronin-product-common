package com.projectronin.product.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Year
import java.time.YearMonth

data class FhirDateTime @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
constructor(@JsonValue val value: String) {

    @get:JsonIgnore
    val parsed: FhirDateTimeValue
        get() = value.toDateTime()
            ?: value.toDate()
            ?: value.toYearMonth()
            ?: value.toYear()
            ?: Unknown(value)

    /**
     * Set of possible _actual_ values
     */
    sealed interface FhirDateTimeValue
    data class DateTimeValue(val value: OffsetDateTime) : FhirDateTimeValue
    data class DateValue(val value: LocalDate) : FhirDateTimeValue
    data class YearMonthValue(val value: YearMonth) : FhirDateTimeValue
    data class YearValue(val value: Year) : FhirDateTimeValue
    data class Unknown(val value: String) : FhirDateTimeValue
}

/**
 * FHIR specifies:
 *  `If hours and minutes are specified, a time zone SHALL be populated.`
 * So we don't need to worry about LocalDateTime
 *
 * The spec says `time zone` but it uses offset, not named zones
 */
private fun String.toDateTime() = runCatching { FhirDateTime.DateTimeValue(OffsetDateTime.parse(this)) }.getOrNull()

private fun String.toDate() = runCatching { FhirDateTime.DateValue(LocalDate.parse(this)) }.getOrNull()

private fun String.toYearMonth() = runCatching { FhirDateTime.YearMonthValue(YearMonth.parse(this)) }.getOrNull()

private fun String.toYear() = runCatching { FhirDateTime.YearValue(Year.parse(this)) }.getOrNull()
