package com.projectronin.product.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.auth.COOKIE_STATE_HEADER
import com.projectronin.product.common.auth.COOKIE_STATE_NAME_PREFIX
import com.projectronin.product.common.auth.COOKIE_STATE_QUERY_PARAMETER
import com.projectronin.product.common.auth.CombinedAuthenticationProvider
import com.projectronin.product.common.auth.IssuerAuthenticationProvider
import com.projectronin.product.common.auth.SekiConfigurationProperties
import com.projectronin.product.common.auth.TrustedIssuerAuthenticationProvider
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.exception.response.api.ErrorResponse
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = false)
@AutoConfiguration
@EnableConfigurationProperties(JwtSecurityProperties::class, SekiConfigurationProperties::class)
@ConditionalOnProperty(prefix = "ronin.product", name = ["security"], matchIfMissing = true)
open class JwtWebfluxSecurityConfig(
    private val securityProperties: JwtSecurityProperties,
    private val sekiConfigurationProperties: SekiConfigurationProperties
) {

    private val logger = KotlinLogging.logger { }

    @Bean
    open fun securityWebFilterChain(
        http: ServerHttpSecurity,
        objectMapper: ObjectMapper,
        tokenAuthenticationManagerResolver: ReactiveAuthenticationManagerResolver<ServerWebExchange>
    ): SecurityWebFilterChain {
        fun handleError(exchange: ServerWebExchange, ex: Exception): Mono<Void> {
            return Mono.defer {
                Mono.just<ServerHttpResponse>(
                    exchange.response
                )
            }.flatMap { response: ServerHttpResponse ->
                logger.warn(ex) { "Authentication failed" }
                response.setStatusCode(HttpStatus.UNAUTHORIZED)
                response.headers.contentType = MediaType.APPLICATION_JSON
                val dataBufferFactory = response.bufferFactory()
                val buffer = dataBufferFactory.wrap(
                    objectMapper.writeValueAsBytes(
                        ErrorResponse.logAndCreateErrorResponse(
                            logger = logger.underlyingLogger,
                            httpStatus = HttpStatus.UNAUTHORIZED,
                            exception = ex,
                            message = "Authentication Error"
                        )
                    )
                )
                response.writeWith(Mono.just<DataBuffer>(buffer))
                    .doOnError { _: Throwable? ->
                        DataBufferUtils.release(
                            buffer
                        )
                    }
            }
        }

        return http
            .cors().and()
            .csrf().disable() // NOTE: csrf recommended disable IFF using token + stateless + no cookie auth
            .formLogin().disable()
            .httpBasic().disable()
            .requestCache().disable()
            .securityMatcher(OrServerWebExchangeMatcher(securityProperties.combinedMatchedPathPatterns().map { PathPatternParserServerWebExchangeMatcher(it) }))
            .authorizeExchange()
            .pathMatchers(*securityProperties.combinedPermittedPathPatterns().toTypedArray()).permitAll()
            .pathMatchers(*securityProperties.combinedSecuredPathPatterns().toTypedArray()).authenticated()
            .and()
            .oauth2ResourceServer { resourceServer ->
                resourceServer
                    .accessDeniedHandler { exchange, exception ->
                        handleError(exchange, exception)
                    }
                    .authenticationEntryPoint { exchange, exception ->
                        handleError(exchange, exception)
                    }
                    .bearerTokenConverter(object : ServerAuthenticationConverter {

                        val delegate = ServerBearerTokenAuthenticationConverter()

                        override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
                            return delegate.convert(exchange)
                                .switchIfEmpty(
                                    Mono.justOrEmpty(exchange.request.headers.getFirst(COOKIE_STATE_HEADER))
                                        .switchIfEmpty(Mono.justOrEmpty(exchange.request.queryParams.getFirst(COOKIE_STATE_QUERY_PARAMETER)))
                                        .flatMap { xState ->
                                            Mono.justOrEmpty(exchange.request.cookies.getFirst("$COOKIE_STATE_NAME_PREFIX$xState"))
                                        }
                                        .map { cookie -> BearerTokenAuthenticationToken(cookie.value) }
                                )
                        }
                    })
                    .authenticationManagerResolver(tokenAuthenticationManagerResolver)
            }
            .build()
    }

    @Bean
    open fun tokenAuthenticationManagerResolver(
        issuerAuthenticationProvider: IssuerAuthenticationProvider
    ): ReactiveAuthenticationManagerResolver<ServerWebExchange> {
        // doing this instead of JwtIssuerReactiveAuthenticationManagerResolver(*issuers) because I want to be able to customize the JwtAuthenticationProviders
        return JwtIssuerReactiveAuthenticationManagerResolver(
            object : ReactiveAuthenticationManagerResolver<String> {
                override fun resolve(context: String?): Mono<ReactiveAuthenticationManager> {
                    return Mono.defer {
                        when (val manager = issuerAuthenticationProvider.resolve(context)) {
                            null -> Mono.empty()
                            else -> Mono.just(object : ReactiveAuthenticationManager {
                                override fun authenticate(authentication: Authentication): Mono<Authentication> {
                                    return Mono.fromCallable { manager.resolve(authentication) }
                                }
                            })
                        }
                    }
                }
            }

        )
    }

    @Bean
    open fun trustedIssuerAuthenticationProvider(
        maybeSekiClient: Optional<SekiClient>
    ): CombinedAuthenticationProvider = TrustedIssuerAuthenticationProvider(
        securityProperties,
        maybeSekiClient.getOrNull()
    )

    @Bean
    open fun sekiClient(
        httpClient: OkHttpClient,
        objectMapper: ObjectMapper
    ): SekiClient? {
        return sekiConfigurationProperties.url?.let { url ->
            SekiClient(
                url,
                httpClient,
                objectMapper
            )
        }
    }

    @Bean
    open fun corsConfigurationSource(): CorsConfigurationSource =
        UrlBasedCorsConfigurationSource().apply {
            securityProperties
                .corsPaths
                .forEach {
                    registerCorsConfiguration(
                        it,
                        CorsConfiguration()
                            .apply {
                                applyPermitDefaultValues()
                                setAllowedMethods(securityProperties.corsAllowedMethods)
                            }
                    )
                }
        }
}
