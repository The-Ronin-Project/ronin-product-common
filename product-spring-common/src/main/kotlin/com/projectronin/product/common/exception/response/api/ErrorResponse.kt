package com.projectronin.product.common.exception.response.api

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import java.time.Instant

/**
 * Intended to be the single basic response that we return on any exception.  It would be acceptable to return
 * a subclass, if (for instance) a list of failed form fields needs to be included or similar.
 *
 * Note that "Null" values will NOT be in the marshalled response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
open class ErrorResponse(
    val httpStatus: HttpStatus,
    val timestamp: Instant? = Instant.now(),
    val status: Int = httpStatus.value(),
    val error: String? = httpStatus.reasonPhrase,
    val exception: String,
    val message: String,
    val detail: String? = null,
    val stacktrace: String? = null,
)
