package com.projectronin.product.common.exception.response.api

import org.springframework.http.HttpStatus

/**
 * Get string representation of exception for error message.
 */
fun Throwable.getExceptionName(): String = javaClass.name

/**
 * Returns the stack trace of an exception if the status of 5xx.
 */
fun optionallyGetStackTrace(httpStatus: HttpStatus, exception: Throwable): String? =
    if (httpStatus.is5xxServerError) exception.stackTraceToString() else null
