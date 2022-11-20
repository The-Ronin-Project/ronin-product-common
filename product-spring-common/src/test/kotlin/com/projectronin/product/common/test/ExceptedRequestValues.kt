package com.projectronin.product.common.test

data class ExceptedRequestValues(
    val method: String = "",
    val requestUrl: String = "",
    val headerMap: Map<String, String> = emptyMap(),
)
