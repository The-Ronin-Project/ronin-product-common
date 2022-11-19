package com.projectronin.product.common.client.exception

import com.projectronin.product.common.exception.response.api.ErrorResponse
import java.io.IOException

open class ServiceClientException(message: String, cause: Throwable? = null, val errorResponse: ErrorResponse? = null) : IOException(message, cause) {
    fun getHttpStatusCode(): Int {
        return errorResponse?.status ?: 0
    }
}
