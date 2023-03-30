package com.projectronin.product.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Simple exception that returns a 400.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
open class BadRequestException(message: String) : RuntimeException(message)
