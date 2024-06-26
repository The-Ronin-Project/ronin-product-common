package com.projectronin.product.common.config

import com.projectronin.product.common.auth.SekiAuthTokenHeaderFilter
import com.projectronin.product.common.auth.SekiConfigurationProperties
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.SekiHealthProvider
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

@AutoConfiguration
@EnableConfigurationProperties(SekiConfigurationProperties::class)
open class SecurityConfiguration(
    val sekiConfigurationProperties: SekiConfigurationProperties
) {

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product", name = ["security"], matchIfMissing = true)
    open fun securityFilterChain(
        http: HttpSecurity,
        sekiClient: SekiClient,
        authenticationFailureHandler: AuthenticationFailureHandler,
        @Value("\${auth.securedPathPatterns:/api/**}") securedPathPatterns: Array<String>
    ): SecurityFilterChain {
        return http
            .cors().and()
            .csrf().disable() // NOTE: csrf recommended disable IFF using token + stateless + no cookie auth
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .securityMatcher(*securedPathPatterns)
            .addFilterAfter(
                SekiAuthTokenHeaderFilter(sekiClient, authenticationFailureHandler),
                AbstractPreAuthenticatedProcessingFilter::class.java // immediately after PreAuth for 'order' location
            )
            .authorizeHttpRequests()
            .anyRequest().permitAll()
            .and()
            .build()
    }

    /*
    Possibility to add alternative auth strategies for local environment and/or testing

    Such strategies would be enabled by setting `ronin.product.security` to `false` or perhaps `passthrough`
    and specifying the real auth require either missing or `enabled`
     */

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product.client", name = ["seki"], matchIfMissing = true)
    open fun getSekiClient(client: OkHttpClient): SekiClient {
        return SekiClient(sekiConfigurationProperties.url!!, client, JsonProvider.objectMapper)
    }

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product.client", name = ["seki"], matchIfMissing = true)
    open fun getSekiHealth(
        sekiClient: SekiClient
    ): SekiHealthProvider = SekiHealthProvider(sekiClient)
}
