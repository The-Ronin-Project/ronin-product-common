package com.projectronin.product.common.client

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface HttpClientFactory {
    fun createClient(configMap: Map<String, Any> = emptyMap()): OkHttpClient
}

/**
 * Creates an OkHttpClient to be used for a ServiceClient
 *   TODO - still a thought experiment
 */
object StdHttpClientFactory : HttpClientFactory {
    const val CONFIG_KEY_CONNECTION_TIMEOUT = "connection.timeout"
    const val CONFIG_KEY_READ_TIMEOUT = "read.timeout"

    // default timeout values to use if creating an okHttpClient
    const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 30_000L
    const val DEFAULT_READ_TIMEOUT_MILLIS = 30_000L
    val DEFAULT_CONFIG_MAP = mapOf(
        CONFIG_KEY_CONNECTION_TIMEOUT to DEFAULT_CONNECT_TIMEOUT_MILLIS,
        CONFIG_KEY_READ_TIMEOUT to DEFAULT_READ_TIMEOUT_MILLIS
    )

    override fun createClient(configMap: Map<String, Any>): OkHttpClient {
        val internalConfigMap = configMap.ifEmpty { DEFAULT_CONFIG_MAP }
        val connectionTimeoutMs = getMillis(internalConfigMap.getOrDefault(CONFIG_KEY_CONNECTION_TIMEOUT, DEFAULT_CONNECT_TIMEOUT_MILLIS))
        val readTimeoutMs = getMillis(internalConfigMap.getOrDefault(CONFIG_KEY_READ_TIMEOUT, DEFAULT_READ_TIMEOUT_MILLIS))
        return OkHttpClient.Builder()
            .connectTimeout(connectionTimeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
            .build()
    }

    private fun getMillis(input: Any): Long {
        return when (input) {
            is Number -> input.toLong() // if input is a Number, assume milliseconds
            is kotlin.time.Duration -> input.inWholeMilliseconds
            is java.time.Duration -> input.toMillis()
            is String -> kotlin.time.Duration.parse(input).inWholeMilliseconds
            else -> throw IllegalArgumentException("Unrecognized class type for timeout value: ${input.javaClass}")
        }
    }
}
