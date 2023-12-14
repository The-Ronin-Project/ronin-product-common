package com.projectronin.product.common.jwtwebfluxsecurityconfigtests

import com.projectronin.product.common.auth.SekiConfigurationProperties
import com.projectronin.product.common.config.JwtSecurityProperties
import com.projectronin.product.common.config.JwtWebfluxSecurityConfig
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpMethod
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

class JwtWebFluxSecurityConfigCorsConfigurationSourceTests {
    private val sekiConfig = mockk<SekiConfigurationProperties>()

    @Test
    fun `default cors configuration`() {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/any-url"))
        val jwtWebfluxSecurityConfig =
            JwtWebfluxSecurityConfig(JwtSecurityProperties(issuers = emptyList()), sekiConfig)

        val corsConfig = jwtWebfluxSecurityConfig.corsConfigurationSource()
        assertThat(corsConfig).isInstanceOf(UrlBasedCorsConfigurationSource::class.java)
        (corsConfig as UrlBasedCorsConfigurationSource)
            .getCorsConfiguration(exchange)!!
            .run {
                assertThat(allowedMethods)
                    .containsExactlyInAnyOrder(
                        HttpMethod.GET.name(),
                        HttpMethod.HEAD.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name()
                    )
            }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/api/v1/management/tenant", "/silly/walks"])
    fun `multiple path cors configuration`(path: String) {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get(path))
        val jwtWebfluxSecurityConfig =
            JwtWebfluxSecurityConfig(
                JwtSecurityProperties(
                    issuers = emptyList(),
                    corsPaths = listOf("/api/**", "/silly/**"),
                    corsAllowedMethods = listOf(HttpMethod.GET.name(), HttpMethod.HEAD.name())
                ),
                sekiConfig
            )

        val corsConfig = jwtWebfluxSecurityConfig.corsConfigurationSource()
        assertThat(corsConfig).isInstanceOf(UrlBasedCorsConfigurationSource::class.java)
        (corsConfig as UrlBasedCorsConfigurationSource)
            .getCorsConfiguration(exchange)!!
            .run {
                assertThat(allowedMethods)
                    .containsExactlyInAnyOrder(
                        HttpMethod.GET.name(),
                        HttpMethod.HEAD.name()
                    )
            }
    }

    @Test
    fun `multiple path cors configuration negative match`() {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/nope"))
        val jwtWebfluxSecurityConfig =
            JwtWebfluxSecurityConfig(
                JwtSecurityProperties(
                    issuers = emptyList(),
                    corsPaths = listOf("/api/**", "/silly/**"),
                    corsAllowedMethods = listOf(HttpMethod.GET.name(), HttpMethod.HEAD.name())
                ),
                sekiConfig
            )

        val corsConfig = jwtWebfluxSecurityConfig.corsConfigurationSource()
        assertThat(corsConfig).isInstanceOf(UrlBasedCorsConfigurationSource::class.java)
        assertThat((corsConfig as UrlBasedCorsConfigurationSource).getCorsConfiguration(exchange)).isNull()
    }
}
