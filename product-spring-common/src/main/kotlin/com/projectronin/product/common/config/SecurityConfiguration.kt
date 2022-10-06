package com.projectronin.product.common.config

import com.projectronin.product.common.auth.SekiAuthTokenHeaderFilter
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.exception.CustomErrorHandler
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
        customErrorHandler: CustomErrorHandler
    ): SecurityFilterChain {
        val authFilter = SekiAuthTokenHeaderFilter(sekiClient, customErrorHandler)
        return http.antMatcher("/api/**")
            .csrf().disable() // NOTE: csrf recommended disable IFF using token + stateless + no cookie auth
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilter(authFilter)
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .build()
    }
}
