package com.projectronin.product.common.auth

import com.projectronin.product.common.testutils.AuthWireMockHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class UnsafeJwtDecoderTest {

    @Test
    fun `should decode a token`() {
        val iat = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val exp = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(10)
        val tokenString = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "foo") { builder ->
            builder.issueTime(Date.from(iat))
            builder.expirationTime(Date.from(exp))
        }
        val jwt = UnsafeJwtDecoder().decode(tokenString)
        assertThat(jwt.headers).isEqualTo(
            mapOf(
                "alg" to "RS256",
                "kid" to AuthWireMockHelper.rsaKey.keyID
            )
        )
        assertThat(jwt.claims).isEqualTo(
            mapOf(
                "sub" to "alice",
                "iat" to iat,
                "exp" to exp,
                "iss" to "foo"
            )
        )
    }

    @Test
    fun `should not complain about no times`() {
        val tokenString = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "foo") { builder ->
            builder.issueTime(null)
            builder.expirationTime(null)
        }
        val jwt = UnsafeJwtDecoder().decode(tokenString)
        assertThat(jwt.headers).isEqualTo(
            mapOf(
                "alg" to "RS256",
                "kid" to AuthWireMockHelper.rsaKey.keyID
            )
        )
        assertThat(jwt.claims).isEqualTo(
            mapOf(
                "sub" to "alice",
                "iss" to "foo"
            )
        )
    }
}
