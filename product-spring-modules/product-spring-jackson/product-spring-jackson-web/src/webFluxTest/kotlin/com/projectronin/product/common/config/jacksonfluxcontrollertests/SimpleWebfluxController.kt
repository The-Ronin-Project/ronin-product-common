package com.projectronin.product.common.config.jacksonfluxcontrollertests

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@RestController
@RequestMapping("/api/test")
class SimpleWebfluxController {

    companion object {
        var receivedBody: Any? = null
    }

    @PostMapping("/sample-object", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun postSampleObject(
        @RequestBody sampleObject: SampleObject
    ): ResponseEntity<SampleObject> {
        receivedBody = sampleObject
        return ResponseEntity
            .ok(sampleObject)
    }

    @PostMapping("/date-holder", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun postDateHolder(
        @RequestBody dateHolder: DateHolder
    ): ResponseEntity<DateHolder> {
        receivedBody = dateHolder
        return ResponseEntity
            .ok(dateHolder)
    }
}

data class DateHolder(
    val a: ZonedDateTime,
    val b: ZonedDateTime,
    val c: LocalDateTime,
    val d: LocalDate
)

data class SampleObject(val a: String, val b: String?)
