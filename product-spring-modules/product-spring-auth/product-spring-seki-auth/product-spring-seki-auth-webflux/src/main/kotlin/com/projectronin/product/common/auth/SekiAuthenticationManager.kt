package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.exception.SekiInvalidTokenException
import mu.KotlinLogging
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

class SekiAuthenticationManager(
    val sekiClient: SekiClient
) : ReactiveAuthenticationManager {

    private val logger = KotlinLogging.logger { }

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.credentials.toString()

        return Mono.just(authToken)
            .filter { it.isNotBlank() }
            .map { token ->
                try {
                    val authResponse = sekiClient.validate(token)
                    authentication.isAuthenticated = true
                    SekiRoninAuthentication(authentication, authResponse.user, authResponse.userSession)
                } catch (e: Exception) {
                    // for any exception, convert it to a AuthenticationException to adhere to
                    //  the original SpringBoot 'authenticate' method signature we are overriding
                    when (e) {
                        is SekiInvalidTokenException -> throw BadCredentialsException("Invalid Seki Token", e)
                        else -> throw AuthenticationServiceException("Unable to verify seki token: ${e.message}", e)
                    }
                }
            }
    }
}
