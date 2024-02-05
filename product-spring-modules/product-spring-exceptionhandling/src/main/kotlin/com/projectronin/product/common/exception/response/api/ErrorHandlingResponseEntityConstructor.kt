package com.projectronin.product.common.exception.response.api

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

interface ErrorHandlingResponseEntityConstructor {

    fun buildResponseEntity(errorResponse: ErrorResponse): ResponseEntity<Any>
}

class DefaultErrorHandlingResponseEntityConstructor : ErrorHandlingResponseEntityConstructor {
    override fun buildResponseEntity(errorResponse: ErrorResponse): ResponseEntity<Any> = ResponseEntity
        .status(errorResponse.httpStatus)
        .contentType(MediaType.APPLICATION_JSON)
        .body(errorResponse)
}
