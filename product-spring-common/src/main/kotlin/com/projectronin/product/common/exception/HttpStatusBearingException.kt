package com.projectronin.product.common.exception

import org.springframework.http.HttpStatus

/**
 * If you throw an exception that implements this interface, it will be handled by `HttpStatusBearingExceptionErrorResponseGenerator`,
 * and the status, message, and detail fields as specified by the interface implementations.
 *
 * @see com.projectronin.product.common.exception.response.HttpStatusBearingExceptionErrorResponseGenerator
 * @see NotFoundException
 */
interface HttpStatusBearingException {

    val httpStatus: HttpStatus

    val errorResponseMessage: String

    val errorResponseDetail: String

}
