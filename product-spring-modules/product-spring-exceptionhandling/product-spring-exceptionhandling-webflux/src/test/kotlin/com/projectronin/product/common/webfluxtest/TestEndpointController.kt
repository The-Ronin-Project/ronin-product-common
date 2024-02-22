package com.projectronin.product.common.webfluxtest

import com.projectronin.auth.RoninAuthentication
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import jakarta.validation.Validator
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@RestController
open class TestEndpointController(
    private val service: TestEndpointService,
    private val validator: Validator
) {

    @PostMapping("/api/test")
    @ResponseStatus(HttpStatus.CREATED)
    fun doTest(
        @RequestBody @Valid
        testBody: TestBody,
        @Suppress("UNUSED_PARAMETER")
        @RequestParam("queryParam", required = false)
        queryParam: Int?,
        roninAuthentication: RoninAuthentication
    ): Mono<ResponseEntity<TestResponse>> {
        return createResponse(testBody, roninAuthentication, HttpStatus.CREATED)
    }

    @PostMapping("/api/testCustomValidation")
    @ResponseStatus(HttpStatus.CREATED)
    fun doCustomValidationTest(
        @RequestBody testBody: TestBody,
        roninAuthentication: RoninAuthentication
    ): Mono<ResponseEntity<TestResponse>> {
        val constraintViolations = validator.validate(testBody)
        if (constraintViolations.isNotEmpty()) {
            throw ConstraintViolationException(constraintViolations)
        }
        return createResponse(testBody, roninAuthentication, HttpStatus.CREATED)
    }

    @GetMapping("/api/testIntGetter/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun doGet(
        @Suppress("UNUSED_PARAMETER")
        @PathVariable("id")
        id: Int,
        roninAuthentication: RoninAuthentication
    ): Mono<ResponseEntity<TestResponse>> {
        return createResponse(TestBody("abc", 3), roninAuthentication, HttpStatus.OK)
    }

    private fun createResponse(@Suppress("UNUSED_PARAMETER") testBody: TestBody, roninAuthentication: RoninAuthentication, status: HttpStatusCode): Mono<ResponseEntity<TestResponse>> {
        return Mono.deferContextual { contextView ->
            val request = contextView.get(ServerWebExchange::class.java).request
            val responseBody = service.getTestResponse().copy(id = "${roninAuthentication.userId}-${roninAuthentication.tenantId}")

            val locationUri = UriComponentsBuilder.fromHttpRequest(request).path("/{id}").buildAndExpand(responseBody.id).toUri()
            Mono.just(
                ResponseEntity.status(status)
                    .location(locationUri) // created(201) + location header
                    .contentType(MediaType.APPLICATION_JSON) // content-type header
                    .body(responseBody)
            )
        }
    }
}

data class TestBody(
    @field:NotBlank
    @field:Size(max = 255)
    val value1: String,
    @field:Min(0)
    @field:Max(10)
    val value2: Int
)

data class TestResponse(
    val id: String
)

@ResponseStatus(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
class FooException : RuntimeException("invalid something or other")
