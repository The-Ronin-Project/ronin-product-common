package com.projectronin.product.common.auth

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class JwtAudienceValidator(private val validAudiences: List<String>) : OAuth2TokenValidator<Jwt> {
    override fun validate(token: Jwt?): OAuth2TokenValidatorResult {
        assert(token != null) { "jwt cannot be null" }
        return when (val audiences = token!!.audience) {
            null -> OAuth2TokenValidatorResult.failure(
                OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "This endpoint requires tokens to have a matching audience",
                    "https://tools.ietf.org/html/rfc6750#section-3.1"
                )
            )

            else -> if (validAudiences.any { validAudience -> audiences.contains(validAudience) }) {
                OAuth2TokenValidatorResult.success()
            } else {
                OAuth2TokenValidatorResult.failure(
                    OAuth2Error(
                        OAuth2ErrorCodes.INVALID_TOKEN,
                        "This endpoint requires tokens to have a matching audience",
                        "https://tools.ietf.org/html/rfc6750#section-3.1"
                    )
                )
            }
        }
    }
}
