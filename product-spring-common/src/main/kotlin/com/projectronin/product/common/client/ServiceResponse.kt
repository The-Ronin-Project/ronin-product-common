package com.projectronin.product.common.client

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

/**
 * Light wrapper around the okhttp3.Response.
 *   because the callers shouldn't really need to know anything about 'okHttp' classes
 */
class ServiceResponse(val httpCode: Int, val body: String, val headerMap: Map<String, String> = emptyMap()) {

    val httpStatus: HttpStatus
    val httpHeaders: HttpHeaders = HttpHeaders()

    init {
        httpStatus = HttpStatus.valueOf(httpCode)
        for (entry in headerMap) {
            httpHeaders.add(entry.key, entry.value)
        }
    }
}
