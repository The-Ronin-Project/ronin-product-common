package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.exception.SekiInvalidTokenException
import com.projectronin.product.common.exception.CustomErrorHandler
import com.projectronin.product.common.exception.response.AuthErrorResponseGenerator
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import javax.servlet.http.HttpServletRequest

private const val AUTH_HEADER_VALUE_PREFIX = "Bearer "

/**
 * Special Filter used to call "Seki validate" for any API calls.
 *
 * NOTE: currently every api call will make an external call to Seki.
 * There is opportunity to add some kind of "limited caching" to avoid extra overhead
 * of seki validate calls.  This would be considered a perf improvement and has
 * a separate backlog story to investigate the validity of this idea:
 * [DASH-3130](https://projectronin.atlassian.net/browse/DASH-3130)
 */
class SekiAuthTokenHeaderFilter(
    sekiClient: SekiClient,
    customErrorHandler: CustomErrorHandler,
) : AbstractPreAuthenticatedProcessingFilter() {

    init {
        // use special authenticationManager to call seki
        this.setAuthenticationManager(SekiAuthenticationManager(sekiClient))
        this.setContinueFilterChainOnUnsuccessfulAuthentication(false) // do NOT continue if there was auth error.
        this.setAuthenticationFailureHandler(customErrorHandler) // ensure auth failures go to our custom error handler
    }

    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest): Any {
        return ""
    }

    /**
     * Get the credentials for the request (which is the Token from the Auth Header)
     */
    override fun getPreAuthenticatedCredentials(request: HttpServletRequest): Any {
        val authValue = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return ""
        if (authValue.startsWith(AUTH_HEADER_VALUE_PREFIX, true)) {
            return authValue.substring(AUTH_HEADER_VALUE_PREFIX.length)
        }
        return ""
    }

    class SekiAuthenticationManager(private val sekiClient: SekiClient) : AuthenticationManager {
        @Throws(AuthenticationException::class)
        override fun authenticate(authentication: Authentication): Authentication {

            // we know the credentials is the token string
            val token = authentication.credentials as String

            if (token.isBlank()) {
                throw PreAuthenticatedCredentialsNotFoundException("Token value was missing or invalid")
            }

            runCatching {
                val authResponse = sekiClient.validate(token)
                authentication.isAuthenticated = true
                return RoninAuthentication(authentication, authResponse.user)
            }.getOrElse {
                // for any exception, convert it to a AuthenticationException to adhere to
                //  the original SpringBoot 'authenticate' method signature we are overriding
                when (it) {
                    is SekiInvalidTokenException -> throw BadCredentialsException("Invalid Seki Token", it)
                    else -> throw AuthenticationServiceException("Unable to verify seki token: ${it.message}", it)
                }
            }
        }
    }
}
