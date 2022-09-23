package com.projectronin.product.common.auth.seki.client.exception

import java.io.IOException

open class SekiClientException(message: String, cause: Throwable? = null) : IOException(message, cause)
