package com.projectronin.product.common.config

import org.springframework.security.config.annotation.web.builders.HttpSecurity

@FunctionalInterface
interface WebMvcFilterChainCustomizer {

    fun customize(http: HttpSecurity): HttpSecurity
}
