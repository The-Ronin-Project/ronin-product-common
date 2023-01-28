package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.exception.SekiInvalidTokenException
import com.projectronin.product.common.exception.auth.CustomAuthenticationFailureHandler
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import org.springframework.web.bind.annotation.ResponseStatus
import javax.servlet.http.HttpServletRequest

private const val AUTH_HEADER_VALUE_PREFIX = "Bearer "
const val COOKIE_STATE_HEADER = "x-state"
const val COOKIE_STATE_NAME_PREFIX = "user_session_token_"

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
    customErrorHandler: CustomAuthenticationFailureHandler,
) : AbstractPreAuthenticatedProcessingFilter() {

    init {
        // use special authenticationManager to call seki
        this.setAuthenticationManager(SekiAuthenticationManager(sekiClient))
//        this.setContinueFilterChainOnUnsuccessfulAuthentication(false) // do NOT continue if there was auth error.
//        this.setAuthenticationFailureHandler(customErrorHandler) // ensure auth failures go to our custom error handler

    }

    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest): Any {
        return ""
    }

    /**
     * Get the credentials for the request (which is the Token from the Auth Header)
     */
    override fun getPreAuthenticatedCredentials(request: HttpServletRequest): String {
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

        private val logger: KLogger = KotlinLogging.logger { }
        @Throws(AuthenticationException::class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        override fun authenticate(authentication: Authentication): Authentication {

            // we know the credentials is the token string
            logger.info("\n\n\n1) getting (token) credentials from authentication")
            val token = authentication.credentials as String
            logger.info("2) got (token) credentials from authentication: $token")

            logger.info("3) seeing if token is blank")
            if (token.isBlank()) {
                throw PreAuthenticatedCredentialsNotFoundException("Token value was missing or invalid")
            }
            logger.info("4) token is not blank")

            try {
                logger.info("5) validating token with Seki")
                val authResponse = sekiClient.validate(token)
                logger.info("6) got valid response from Seki: $authResponse")
                authentication.isAuthenticated = true
                logger.info("7) instantiating ronin authentication object")
                return RoninAuthentication(authentication, authResponse.user, authResponse.userSession)
            } catch (e: Exception) {
                // for any exception, convert it to a AuthenticationException to adhere to
                //  the original SpringBoot 'authenticate' method signature we are overriding
                logger.info("_1) handling exception by casting to AuthenticationException")
                when (e) {
                    is SekiInvalidTokenException -> throw BadCredentialsException("Invalid Seki Token", e)
                    else -> throw AuthenticationServiceException("Unable to verify seki token: ${e.message}", e)
                }
            }
        }
    }
}
