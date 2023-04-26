package com.projectronin.product.common.config

import com.projectronin.product.common.auth.AuthErrorResponseGenerator
import com.projectronin.product.common.auth.SekiAuthenticationManager
import com.projectronin.product.common.auth.SekiConfigurationProperties
import com.projectronin.product.common.auth.SekiSecurityContextRepository
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.SekiHealthProvider
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Flux

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Configuration
@EnableConfigurationProperties(SekiConfigurationProperties::class)
open class SecurityConfiguration(val sekiConfigurationProperties: SekiConfigurationProperties) {

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product", name = ["security"], matchIfMissing = true)
    open fun securityWebFilterChain(
        http: ServerHttpSecurity,
        sekiAuthenticationManager: SekiAuthenticationManager,
        sekiSecurityContextRepository: SekiSecurityContextRepository,
        responseGenerator: AuthErrorResponseGenerator,
        @Value("\${auth.securedPathPatterns:/api/**}") securedPathPatterns: Array<String>
    ): SecurityWebFilterChain {
        return http
            .cors().and()
            .csrf().disable() // NOTE: csrf recommended disable IFF using token + stateless + no cookie auth
            .formLogin().disable()
            .httpBasic().disable()
            .requestCache().disable()
            .authenticationManager(sekiAuthenticationManager)
            .securityContextRepository(sekiSecurityContextRepository)
            .addFilterBefore(
                { exchange, chain ->
                    chain.filter(exchange)
                        .onErrorResume(AuthenticationException::class.java) { ex ->
                            val response = exchange.response
                            response.apply {
                                statusCode = HttpStatus.UNAUTHORIZED
                                headers.contentType = MediaType.APPLICATION_JSON
                            }

                            response.writeWith(
                                Flux.just(
                                    DefaultDataBufferFactory().wrap(
                                        responseGenerator.responseBody(ex)
                                    )
                                )
                            )
                        }
                },
                SecurityWebFiltersOrder.AUTHENTICATION
            )
            .authorizeExchange()
            .pathMatchers(*securedPathPatterns).authenticated()
            .and()
            .build()
    }

    /*
    Possibility to add alternative auth strategies for local environment and/or testing

    Such strategies would be enabled by setting `ronin.product.security` to `false` or perhaps `passthrough`
    and specifying the real auth require either missing or `enabled`
     */

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product.client", name = ["seki"], matchIfMissing = true)
    open fun getSekiClient(client: OkHttpClient): SekiClient {
        return SekiClient(sekiConfigurationProperties.url!!, client, JsonProvider.objectMapper)
    }

    @Bean
    open fun sekiAuthenticationManager(sekiClient: SekiClient): SekiAuthenticationManager {
        return SekiAuthenticationManager(sekiClient)
    }

    @Bean
    open fun sekiSecurityContextRepository(sekiAuthenticationManager: SekiAuthenticationManager): SekiSecurityContextRepository {
        return SekiSecurityContextRepository(sekiAuthenticationManager)
    }

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product.client", name = ["seki"], matchIfMissing = true)
    open fun getSekiHealth(
        sekiClient: SekiClient
    ): SekiHealthProvider = SekiHealthProvider(sekiClient)
}
