package com.projectronin.product.common.testconfigs

import com.projectronin.product.common.auth.SekiConfigurationProperties
import com.projectronin.product.common.config.JwtSecurityProperties
import com.projectronin.product.common.config.SEKI_ISSUER_NAME
import com.projectronin.product.common.testutils.AuthWireMockHelper
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.reactive.server.WebTestClientBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

private val AuthWireMockHelper.validProperties
    get() = JwtSecurityProperties(
        securedPathPatterns = listOf("/api/**"),
        detailedErrors = true,
        issuers = listOf("http://127.0.0.1:$wireMockPort", SEKI_ISSUER_NAME, "http://127.0.0.1:$wireMockPort/second-issuer"),
        sekiSharedSecret = sekiSharedSecret,
        validAudiences = listOf("http://127.0.0.1:$wireMockPort")
    )

private val AuthWireMockHelper.sekiProperties
    get() = SekiConfigurationProperties(
        url = "http://127.0.0.1:$wireMockPort/seki"
    )

@TestConfiguration
open class AudiencePropertiesConfig {
    @Primary
    @Bean
    open fun jwtSecurityProperties(): JwtSecurityProperties {
        return AuthWireMockHelper.validProperties
    }

    @Primary
    @Bean
    open fun sekConfigurationProperties(): SekiConfigurationProperties {
        return AuthWireMockHelper.sekiProperties
    }

    @Bean
    open fun customizer(): WebTestClientBuilderCustomizer? {
        return WebTestClientBuilderCustomizer { builder: WebTestClient.Builder ->
            builder.responseTimeout(Duration.ofSeconds(30L))
        }
    }
}
