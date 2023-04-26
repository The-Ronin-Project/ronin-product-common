package com.projectronin.product.common.testutils

import com.projectronin.product.common.auth.AuthenticationProvider
import com.projectronin.product.common.auth.IssuerAuthenticationProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
open class JwtAuthMockConfig {

    @Bean
    @Primary
    open fun issuerAuthenticationProvider(): IssuerAuthenticationProvider {
        return object : IssuerAuthenticationProvider {
            override fun resolve(issuerUrl: String?): AuthenticationProvider {
                return JwtAuthMockHelper.currentAuthenticationProvider
            }
        }
    }
}
