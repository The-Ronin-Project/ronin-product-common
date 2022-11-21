package com.projectronin.product.common.client

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

/**
 * Basic Http Response object.
 *   because the callers shouldn't really need to know anything about 'okHttp' classes
 */
// TODO: review - may refactor to implement ClientHttpResponse interface
//     toggling thoughts on how many 'ties' the client code should have with Spring stuff.
class ServiceResponse(val httpCode: Int, val body: String, val headerMap: Map<String, String> = emptyMap()) {

    val httpStatus: HttpStatus = HttpStatus.valueOf(httpCode)
    val httpHeaders: HttpHeaders = HttpHeaders()

    init {
        for (entry in headerMap) {
            httpHeaders.add(entry.key, entry.value)
        }
    }
}
