package com.projectronin.product.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Simple exception that returns a 403.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
open class ForbiddenException(message: String = HttpStatus.FORBIDDEN.reasonPhrase) : RuntimeException(message)
