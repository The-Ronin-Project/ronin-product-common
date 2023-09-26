package com.projectronin.product.common.auth

import com.projectronin.auth.RoninAuthentication
import jakarta.annotation.PostConstruct
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class SimpleWebfluxController {

    companion object {
        var receivedAuth: RoninAuthentication? = null
    }

    @PostConstruct
    fun something() {
        println("foo")
    }

    @GetMapping("/sample-object", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getSampleObject(auth: RoninAuthentication): ResponseEntity<SampleObject> {
        receivedAuth = auth
        return ResponseEntity
            .ok(SampleObject("foo", "bar"))
    }
}

data class SampleObject(val a: String, val b: String?)
