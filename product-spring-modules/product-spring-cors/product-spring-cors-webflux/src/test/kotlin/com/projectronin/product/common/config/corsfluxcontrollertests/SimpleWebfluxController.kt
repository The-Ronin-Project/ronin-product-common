package com.projectronin.product.common.config.corsfluxcontrollertests

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class SimpleWebfluxController {

    @GetMapping("/sample-object", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun postSampleObject(): ResponseEntity<Any> {
        return ResponseEntity
            .ok("""{"foo": null}""")
    }
}
