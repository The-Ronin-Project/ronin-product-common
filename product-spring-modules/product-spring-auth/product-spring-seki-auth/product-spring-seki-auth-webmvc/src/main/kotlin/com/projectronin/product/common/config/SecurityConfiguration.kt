package com.projectronin.product.common.config

import com.projectronin.product.common.auth.SekiAuthTokenHeaderFilter
import com.projectronin.product.common.auth.seki.client.SekiClient
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFailureHandler

@Configuration
open class SecurityConfiguration {

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
            .addFilter(SekiAuthTokenHeaderFilter(sekiClient, authenticationFailureHandler))
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
    open fun getSekiClient(@Value("\${seki.url}") sekiUrl: String, client: OkHttpClient): SekiClient {
        return SekiClient(sekiUrl, client, JsonProvider.objectMapper)
    }
}
