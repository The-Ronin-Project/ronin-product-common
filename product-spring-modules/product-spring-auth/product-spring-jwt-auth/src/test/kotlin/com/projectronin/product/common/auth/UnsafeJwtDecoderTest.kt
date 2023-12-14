package com.projectronin.product.common.auth

import com.projectronin.auth.RoninClaimsAuthentication
import com.projectronin.product.common.testutils.AuthWireMockHelper
import com.projectronin.product.common.testutils.wiremockJwtAuthToken
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
        val tokenString = wiremockJwtAuthToken {
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
        val tokenString = wiremockJwtAuthToken {
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
