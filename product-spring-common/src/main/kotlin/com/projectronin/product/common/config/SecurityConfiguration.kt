package com.projectronin.product.common.config

import CustomAccessDeniedHandler
import com.projectronin.product.common.auth.SekiAuthTokenHeaderFilter
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.exception.auth.CustomAuthenticationFailureHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
open class SecurityConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product", name = ["security"], matchIfMissing = true)
    open fun securityFilterChain(
        http: HttpSecurity,
        sekiClient: SekiClient,
    ): SecurityFilterChain {
        return http
            .cors().and()
            .csrf().disable() // NOTE: csrf recommended disable IFF using token + stateless + no cookie auth
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .antMatcher("/api/**")
            .addFilter(SekiAuthTokenHeaderFilter(sekiClient, CustomAuthenticationFailureHandler()))
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .exceptionHandling()
//            .accessDeniedHandler(CustomAccessDeniedHandler())
            .and()
            .build()
    }

    /*
    Possibility to add alternative auth strategies for local environment and/or testing

    Such strategies would be enabled by setting `ronin.product.security` to `false` or perhaps `passthrough`
    and specifying the real auth require either missing or `enabled`
     */
}
