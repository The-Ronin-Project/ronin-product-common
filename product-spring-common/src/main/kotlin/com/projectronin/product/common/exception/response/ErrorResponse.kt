package com.projectronin.product.common.exception.response

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus

// note that "Null" values will NOT be in the marshalled response.
@JsonInclude(JsonInclude.Include.NON_NULL)
class ErrorResponse(
    @JsonIgnore
    val httpStatus: HttpStatus
) {
    var timestamp: String? = null
    var status = 0
    var error: String? = null
    var exception: String? = null
    var message: String? = null
    var detail: String? = null
    var stacktrace: String? = null
}
