package com.projectronin.product.common.testutils

import com.projectronin.product.common.auth.AuthenticationProvider
import com.projectronin.product.common.auth.CombinedAuthenticationProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
open class JwtAuthMockConfig {

    @Bean
    @Primary
    open fun issuerAuthenticationProvider(): CombinedAuthenticationProvider {
        return object : CombinedAuthenticationProvider {
            override fun resolve(issuerUrl: String?): AuthenticationProvider {
                return JwtAuthMockHelper.currentAuthenticationProvider
            }

            override fun forToken(token: String): AuthenticationProvider {
                return JwtAuthMockHelper.currentAuthenticationProvider
            }
        }
    }
}
