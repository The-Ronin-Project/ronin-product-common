package com.projectronin.product.common.auth.seki.client.exception

import java.io.IOException
import com.projectronin.product.common.auth.seki.client.SekiClient

/**
 * Exception representing a generic error with the [SekiClient]
 */
open class SekiClientException(message: String, cause: Throwable? = null) : IOException(message, cause)
