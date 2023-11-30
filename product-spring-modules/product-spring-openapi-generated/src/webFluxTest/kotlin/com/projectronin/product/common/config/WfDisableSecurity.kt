package com.projectronin.product.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
open class WfDisableSecurity {

    @Bean
    open fun securityWebFilterChain(
        http: ServerHttpSecurity,
    ): SecurityWebFilterChain? {
        return http
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .requestCache().disable()
            .authorizeExchange()
            .anyExchange().permitAll()
            .and()
            .build()
    }
}
