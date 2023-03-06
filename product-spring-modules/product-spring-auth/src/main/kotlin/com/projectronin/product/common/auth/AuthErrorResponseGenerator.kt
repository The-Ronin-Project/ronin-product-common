package com.projectronin.product.common.auth

fun interface AuthErrorResponseGenerator {
    fun responseBody(throwable: Throwable): ByteArray
}
