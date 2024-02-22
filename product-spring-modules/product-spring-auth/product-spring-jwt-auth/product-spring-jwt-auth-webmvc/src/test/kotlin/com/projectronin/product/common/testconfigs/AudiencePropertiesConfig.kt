package com.projectronin.product.common.testconfigs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.projectronin.product.common.auth.SekiConfigurationProperties
import com.projectronin.product.common.config.JwtSecurityProperties
import com.projectronin.product.common.config.SEKI_ISSUER_NAME
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
open class AudiencePropertiesConfig {

    val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

    private val wireMockPort
        get() = wireMockServer.port()

    @Primary
    @Bean
    open fun jwtSecurityProperties(): JwtSecurityProperties {
        return JwtSecurityProperties(
            issuers = listOf("http://localhost:$wireMockPort", SEKI_ISSUER_NAME, "http://localhost:$wireMockPort/second-issuer"),
            sekiSharedSecret = sekiSharedSecret,
            securedPathPatterns = listOf("/api/**"),
            validAudiences = listOf("http://localhost:$wireMockPort")
        )
    }

    @Primary
    @Bean
    open fun sekConfigurationProperties(): SekiConfigurationProperties {
        return SekiConfigurationProperties(
            url = "http://localhost:$wireMockPort/seki"
        )
    }

    @PostConstruct
    fun startWireMock() {
        wireMockServer.start()
        WireMock.configureFor(wireMockServer.port())
    }

    @PreDestroy
    fun stopWireMock() {
        wireMockServer.stop()
    }
}

val sekiSharedSecret = "23jB5lMDPhXXahTBosjuUFhoMK0joALW0tQDa5ydqS5QyoPcA8tev4BVsoZltej5"
