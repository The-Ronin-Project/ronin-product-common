package com.projectronin.product.common.config

import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

private const val DEFAULT_HTTP_CONNECTION_TIMEOUT = 15000L
private const val DEFAULT_HTTP_READ_TIMEOUT = 15000L

@Configuration
open class HttpClientConfiguration {

    // NOTE:  tbd for 'better' home of httpClient might live.
    @Bean
    open fun getHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(DEFAULT_HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(DEFAULT_HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
    }
}
