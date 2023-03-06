package com.projectronin.product.common.auth

import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class SekiSecurityContextRepository(
    private val authenticationManager: SekiAuthenticationManager
) : ServerSecurityContextRepository {
    override fun save(exchange: ServerWebExchange?, context: SecurityContext?): Mono<Void> {
        throw UnsupportedOperationException("Security context saving not supported")
    }

    override fun load(swe: ServerWebExchange): Mono<SecurityContext> {
        return Mono.justOrEmpty(swe.request.headers.getFirst(HttpHeaders.AUTHORIZATION))
            .filter { authHeader -> authHeader.startsWith(AUTH_HEADER_VALUE_PREFIX) }
            .map { authHeader -> authHeader.substring(AUTH_HEADER_VALUE_PREFIX.length) }
            .switchIfEmpty(
                Mono.justOrEmpty(swe.request.headers.getFirst(COOKIE_STATE_HEADER))
                    .flatMap { xState ->
                        Mono.justOrEmpty(swe.request.cookies.getFirst("$COOKIE_STATE_NAME_PREFIX$xState"))
                    }
                    .map { cookie -> cookie.value }
            )
            .switchIfEmpty(Mono.error(PreAuthenticatedCredentialsNotFoundException("Token value was missing or invalid")))
            .flatMap { authToken ->
                val auth: Authentication = PreAuthenticatedAuthenticationToken(authToken, authToken)
                authenticationManager.authenticate(auth).map { SecurityContextImpl(it) }
            }
    }
}
