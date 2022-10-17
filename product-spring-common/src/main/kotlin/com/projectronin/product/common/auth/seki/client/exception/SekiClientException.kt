package com.projectronin.product.common.auth.seki.client.exception

import com.projectronin.product.common.auth.seki.client.SekiClient
import java.io.IOException

/**
 * Exception representing a generic error with the [SekiClient]
 */
open class SekiClientException(message: String, cause: Throwable? = null) : IOException(message, cause)
