package com.projectronin.product.common.config

import com.projectronin.product.common.auth.seki.client.SekiClient
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

private const val DEFAULT_HTTP_CONNECTION_TIMEOUT = 15000L
private const val DEFAULT_HTTP_READ_TIMEOUT = 15000L

@Configuration
open class HttpClientConfiguration {

    @Bean
    open fun getHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(DEFAULT_HTTP_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(DEFAULT_HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
    }

    @Bean
    open fun getSekiClient(@Value("\${seki.url}") sekiUrl: String, client: OkHttpClient): SekiClient {
        return SekiClient(sekiUrl, client, JsonProvider.objectMapper)
    }
}
