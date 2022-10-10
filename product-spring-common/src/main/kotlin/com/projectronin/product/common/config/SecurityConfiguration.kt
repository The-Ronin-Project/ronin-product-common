package com.projectronin.product.common.config

import com.projectronin.product.common.auth.SekiAuthTokenHeaderFilter
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.exception.auth.CustomAuthenticationFailureHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
open class SecurityConfiguration {

    @Bean
    open fun securityFilterChain(
        http: HttpSecurity,
        sekiClient: SekiClient,
    ): SecurityFilterChain {
        return http
            .csrf().disable() // NOTE: csrf recommended disable IFF using token + stateless + no cookie auth
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .antMatcher("/api/**")
            .addFilter(SekiAuthTokenHeaderFilter(sekiClient, CustomAuthenticationFailureHandler()))
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .build()
    }
}
