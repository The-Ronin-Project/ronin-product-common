package com.projectronin.product.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Simple exception that returns a 401.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
open class UnauthorizedException(message: String) : RuntimeException(message)
