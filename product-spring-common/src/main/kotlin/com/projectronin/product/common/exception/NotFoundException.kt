package com.projectronin.product.common.exception

import org.springframework.http.HttpStatus

/**
 * Simple exception that returns a 404.
 */
open class NotFoundException(id: String) : RuntimeException(), HttpStatusBearingException {
    override val httpStatus: HttpStatus = HttpStatus.NOT_FOUND

    override val errorResponseMessage: String = "Not Found"

    override val errorResponseDetail: String = "Item was not found: $id"
}
