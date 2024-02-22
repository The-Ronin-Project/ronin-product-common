package com.projectronin.product.common.auth

import com.projectronin.auth.RoninClaimsAuthentication
import com.projectronin.test.jwt.generateRandomRsa
import com.projectronin.test.jwt.jwtAuthToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class UnsafeJwtDecoderTest {

    val rsaKey = generateRandomRsa()

    @Test
    fun `should decode a token`() {
        val iat = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val exp = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(10)
        val tokenString = jwtAuthToken(rsaKey, "https://example.org") {
            withTokenCustomizer {
                issueTime(Date.from(iat))
                    .expirationTime(Date.from(exp))
                    .claim(RoninClaimsAuthentication.roninClaimsKey, null)
                    .issuer("foo")
            }
        }
        val jwt = UnsafeJwtDecoder().decode(tokenString)
        assertThat(jwt.headers).isEqualTo(
            mapOf(
                "alg" to "RS256",
                "kid" to rsaKey.keyID
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
        val tokenString = jwtAuthToken(rsaKey, "https://example.org") {
            withTokenCustomizer {
                issueTime(null)
                    .expirationTime(null)
                    .claim(RoninClaimsAuthentication.roninClaimsKey, null)
                    .issuer("foo")
            }
        }
        val jwt = UnsafeJwtDecoder().decode(tokenString)
        assertThat(jwt.headers).isEqualTo(
            mapOf(
                "alg" to "RS256",
                "kid" to rsaKey.keyID
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
