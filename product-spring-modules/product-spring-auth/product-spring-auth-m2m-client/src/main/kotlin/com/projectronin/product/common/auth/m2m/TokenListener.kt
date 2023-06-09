package com.projectronin.product.common.auth.m2m

fun interface TokenListener {
    fun tokenChanged(newToken: TokenResponse)
}
