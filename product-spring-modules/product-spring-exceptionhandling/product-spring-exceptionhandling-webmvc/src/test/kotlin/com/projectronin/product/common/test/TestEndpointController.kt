package com.projectronin.product.common.test

import com.projectronin.auth.RoninAuthentication
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import jakarta.validation.Validator
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

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
        @Suppress("UNUSED_PARAMETER") authentication: RoninAuthentication,
        @Suppress("UNUSED_PARAMETER")
        @RequestParam("queryParam", required = false)
        queryParam: Int?
    ): ResponseEntity<TestResponse> {
        return createResponse(testBody)
    }

    @PostMapping("/api/testCustomValidation")
    @ResponseStatus(HttpStatus.CREATED)
    fun doCustomValidationTest(
        @RequestBody testBody: TestBody,
        @Suppress("UNUSED_PARAMETER") authentication: RoninAuthentication
    ): ResponseEntity<TestResponse> {
        val constraintViolations = validator.validate(testBody)
        if (constraintViolations.isNotEmpty()) {
            throw ConstraintViolationException(constraintViolations)
        }
        return createResponse(testBody)
    }

    @GetMapping("/api/testIntGetter/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun doGet(
        @Suppress("UNUSED_PARAMETER")
        @PathVariable("id")
        id: Int,
        @Suppress("UNUSED_PARAMETER") authentication: RoninAuthentication
    ): ResponseEntity<TestResponse> {
        return createResponse(TestBody("abc", 3))
    }

    private fun createResponse(@Suppress("UNUSED_PARAMETER") testBody: TestBody): ResponseEntity<TestResponse> {
        val responseBody = service.getTestResponse()

        val locationUri =
            ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(responseBody.id).toUri()
        return ResponseEntity
            .created(locationUri) // created(201) + location header
            .contentType(MediaType.APPLICATION_JSON) // content-type header
            .body(responseBody)
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
