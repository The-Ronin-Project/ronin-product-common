package com.projectronin.product.common.exception.response.api

/**
 * A convenience class that just specifies an error message and detail message.  Meant to be used by
 * subclasses of `AbstractSimpleErrorHandlingEntityBuilder` to report the message they want to return.
 */
data class ErrorMessageInfo(val message: String, val detail: String?)
