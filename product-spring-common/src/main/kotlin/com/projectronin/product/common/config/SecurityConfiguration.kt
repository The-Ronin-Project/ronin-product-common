package com.projectronin.product.common.config

import com.projectronin.product.common.auth.SekiAuthTokenHeaderFilter
import com.projectronin.product.common.auth.seki.client.SekiClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
open class SecurityConfiguration(private val sekiClient: SekiClient) {

    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val authFilter = SekiAuthTokenHeaderFilter(sekiClient)
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
