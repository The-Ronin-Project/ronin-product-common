package com.projectronin.product.common.client

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

// TODO - not used (presently) -
//    wanted to physically see what impl would look like with the interface
class ServiceResponse2(httpCode: Int, val bodyText: String, val headerMap: Map<String, String> = emptyMap()) : ClientHttpResponse {

    val httpStatus: HttpStatus
    val httpHeaders: HttpHeaders = HttpHeaders()

    init {
        httpStatus = HttpStatus.valueOf(httpCode)
        for (entry in headerMap) {
            httpHeaders.add(entry.key, entry.value)
        }
    }

    @Throws(IOException::class)
    override fun getStatusCode(): HttpStatus {
        return httpStatus
    }

    @Throws(IOException::class)
    override fun getRawStatusCode(): Int {
        return httpStatus.value()
    }

    @Throws(IOException::class)
    override fun getStatusText(): String {
        return httpStatus.reasonPhrase
    }

    override fun close() {
        // No-Op - any necessary close on the response should have already been handled.
    }

    @Throws(IOException::class)
    override fun getBody(): InputStream {
        return ByteArrayInputStream(bodyText.toByteArray(StandardCharsets.UTF_8))
    }

    override fun getHeaders(): HttpHeaders {
        return httpHeaders
    }
}
