package com.projectronin.product.common.config

import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import java.util.concurrent.TimeUnit

private const val DEFAULT_HTTP_CONNECTION_TIMEOUT = 15000L
private const val DEFAULT_HTTP_READ_TIMEOUT = 15000L

@AutoConfiguration
open class HttpClientConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product.client", name = ["okhttp"], matchIfMissing = true)
    open fun getHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(DEFAULT_HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(DEFAULT_HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
    }
}
