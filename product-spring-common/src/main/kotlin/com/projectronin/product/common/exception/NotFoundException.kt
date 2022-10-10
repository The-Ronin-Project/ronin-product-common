package com.projectronin.product.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Simple exception that returns a 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
open class NotFoundException(id: String) : RuntimeException("Item was not found: $id") {
}
