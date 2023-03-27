package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.exception.SekiInvalidTokenException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import org.springframework.security.web.util.matcher.RequestMatcher

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
    customErrorHandler: AuthenticationFailureHandler
) : AbstractAuthenticationProcessingFilter(AlwaysAuthorizeRequestMatcher) {

    init {
        // use special authenticationManager to call seki
        this.setAuthenticationManager(SekiAuthenticationManager(sekiClient))
        this.setAuthenticationFailureHandler(customErrorHandler) // ensure auth failures go to our custom error handler
        this.setAuthenticationSuccessHandler(NoOpAuthenticationSuccessHandler) // no extra operation required for success
    }

    /**
     * @inheritDoc
     */
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse?): Authentication {
        val credentials = getCredentials(request)
        // Note: we do not technically need a 'PreAuthenticatedAuthenticationToken',
        //   just need a 'Authentication' for the authManager. and it was handily available.
        val authenticationRequest = PreAuthenticatedAuthenticationToken("", credentials).apply {
            details = authenticationDetailsSource.buildDetails(request)
        }
        return authenticationManager.authenticate(authenticationRequest)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        chain: FilterChain?,
        authResult: Authentication?
    ) {
        super.successfulAuthentication(request, response, chain, authResult)
        // want to call chain.doFilter _AFTER_ 'successfulAuthentication'
        //  (so REST endpoints can access the authorization object)
        chain?.doFilter(request, response)
    }

    /**
     * Get the credentials for the request (which is the Token from the Auth Header)
     */
    private fun getCredentials(request: HttpServletRequest): String {
        // Header check
        request.getHeader(HttpHeaders.AUTHORIZATION)?.let { header ->
            if (header.startsWith(AUTH_HEADER_VALUE_PREFIX, true)) {
                return header.substring(AUTH_HEADER_VALUE_PREFIX.length)
            }
        }

        // Cookie check
        request.getHeader(COOKIE_STATE_HEADER)?.let { state ->
            request.cookies?.find { it.name == "$COOKIE_STATE_NAME_PREFIX$state" }?.run { return value }
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

            try {
                val authResponse = sekiClient.validate(token)
                authentication.isAuthenticated = true
                return SekiRoninAuthentication(authentication, authResponse.user, authResponse.userSession)
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

    // simple RequestMatcher to indicate to always authorize
    private object AlwaysAuthorizeRequestMatcher : RequestMatcher {
        override fun matches(request: HttpServletRequest?): Boolean {
            return true
        }
    }

    // Nothing extra needed on an auth success.
    //   SpringBoot default wants to '302-redirect', which is undesired for our service needs.
    private object NoOpAuthenticationSuccessHandler : AuthenticationSuccessHandler {
        override fun onAuthenticationSuccess(
            request: HttpServletRequest?,
            response: HttpServletResponse?,
            authentication: Authentication?
        ) { /*  do nothing  */ }
    }
}
