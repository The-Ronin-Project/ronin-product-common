package com.projectronin.validation.clinical.data.client.work.exception

import com.projectronin.product.common.client.ServiceResponse
import java.io.IOException

open class ServiceClientException(message: String, cause: Throwable? = null, val serviceResponse: ServiceResponse? = null) : IOException(message, cause) {
    fun getHttpStatusCode(): Int {
        return serviceResponse?.httpCode ?: 0
    }
}
