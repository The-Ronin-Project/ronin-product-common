package com.projectronin.product.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.auth.AuthContextServletFilter
import com.projectronin.product.common.auth.COOKIE_STATE_HEADER
import com.projectronin.product.common.auth.COOKIE_STATE_NAME_PREFIX
import com.projectronin.product.common.auth.COOKIE_STATE_QUERY_PARAMETER
import com.projectronin.product.common.auth.CombinedAuthenticationProvider
import com.projectronin.product.common.auth.IssuerAuthenticationProvider
import com.projectronin.product.common.auth.SekiConfigurationProperties
import com.projectronin.product.common.auth.TrustedIssuerAuthenticationProvider
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.exception.response.api.getExceptionName
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@AutoConfiguration
@EnableConfigurationProperties(JwtSecurityProperties::class, SekiConfigurationProperties::class)
@EnableMethodSecurity
@ConditionalOnProperty(prefix = "ronin.product", name = ["security"], matchIfMissing = true)
open class JwtWebMvcSecurityConfig(
    private val securityProperties: JwtSecurityProperties,
    private val sekiConfigurationProperties: SekiConfigurationProperties,
    private val maybeFilterChainCustomizer: Optional<WebMvcFilterChainCustomizer>
) {

    private val logger = KotlinLogging.logger { }

    @Bean
    open fun securityFilterChain(
        http: HttpSecurity,
        objectMapper: ObjectMapper,
        tokenAuthenticationManagerResolver: AuthenticationManagerResolver<HttpServletRequest>
    ): SecurityFilterChain {
        fun handleError(response: HttpServletResponse, ex: Exception) {
            logger.warn(ex) { "Authentication failed" }

            response.status = HttpStatus.UNAUTHORIZED.value()
            response.contentType = MediaType.APPLICATION_JSON.toString()
            response.outputStream.write(
                objectMapper.writeValueAsBytes(
                    ErrorResponse(
                        httpStatus = HttpStatus.UNAUTHORIZED,
                        exception = if (securityProperties.detailedErrors) ex.getExceptionName() else "Exception",
                        message = "Authentication Error",
                        detail = if (securityProperties.detailedErrors) ex.message else "Unauthorized"
                    )
                )
            )
        }

        return http
            .cors().and()
            .calculateCsrfSetting()
            .httpBasic().disable()
            .formLogin().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.valueOf(securityProperties.sessionCreationPolicy)).and()
            .securityMatcher(*securityProperties.combinedMatchedPathPatterns().toTypedArray())
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(*securityProperties.combinedPermittedPathPatterns().toTypedArray()).permitAll()
                    .requestMatchers(*securityProperties.combinedSecuredPathPatterns().toTypedArray()).authenticated()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer
                    .accessDeniedHandler { _, response, exception ->
                        handleError(response, exception)
                    }
                    .authenticationEntryPoint { _, response, exception ->
                        handleError(response, exception)
                    }
                    .bearerTokenResolver(object : BearerTokenResolver {

                        private val delegate = DefaultBearerTokenResolver()

                        override fun resolve(request: HttpServletRequest): String? {
                            return delegate.resolve(request) ?: tryCookieAuth(request)
                        }

                        private fun tryCookieAuth(request: HttpServletRequest): String? {
                            return (request.getHeader(COOKIE_STATE_HEADER) ?: request.getParameter(COOKIE_STATE_QUERY_PARAMETER))
                                ?.let { state ->
                                    request.cookies?.find { it.name == "$COOKIE_STATE_NAME_PREFIX$state" }?.value
                                }
                        }
                    })
                    .authenticationManagerResolver(tokenAuthenticationManagerResolver)
            }
            .addFilterAfter(
                AuthContextServletFilter(),
                SecurityContextHolderAwareRequestFilter::class.java // after the authenticated user (principal) is set
            )
            .customize()
            .build()
    }

    @Bean
    open fun tokenAuthenticationManagerResolver(
        issuerAuthenticationProvider: IssuerAuthenticationProvider
    ): AuthenticationManagerResolver<HttpServletRequest> {
        // doing this instead of JwtIssuerAuthenticationManagerResolver(*issuers) because I want to be able to customize the JwtAuthenticationProviders
        return JwtIssuerAuthenticationManagerResolver { issuerUrl ->
            when (val provider = issuerAuthenticationProvider.resolve(issuerUrl)) {
                null -> null
                else -> AuthenticationManager { authentication -> provider.resolve(authentication) }
            }
        }
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

    private fun HttpSecurity.customize(): HttpSecurity {
        return maybeFilterChainCustomizer.map { actualCustomizer -> actualCustomizer.customize(this) }
            .orElse(this)
    }

    private fun HttpSecurity.calculateCsrfSetting(): HttpSecurity {
        return if (securityProperties.disableCsrf) {
            csrf().disable() // NOTE: csrf recommended disable IFF using token + stateless + no cookie auth
        } else {
            this
        }
    }
}
