package com.projectronin.product.common.testutils

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Deprecated("Use functions in ronin-common:jwt-auth-test.  Seki no longer supported.")
@Suppress("DEPRECATION")
object AuthMockHelper {

    val rsaKey: RSAKey = AuthKeyGenerator.generateRandomRsa()

    const val sekiSharedSecret = "23jB5lMDPhXXahTBosjuUFhoMK0joALW0tQDa5ydqS5QyoPcA8tev4BVsoZltej5"
    const val defaultSekiToken =
        "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTZWtpIiwidGVuYW50aWQiOiJlamgzajk1aCIsInN1YiI6IjRmMWMyM2Q2LTUzYTQtNDE3Yy1hYTA4LWYzMTAwODBjNjM5OCIsImlhdCI6MTY4MjQzNzY4NSwianRpIjoiZDczNDJkODEtYWYwOC00YmYxLWEyMTItNTJiMmNiNGQ2YWYyIn0.Xq7TYwfP71cvcwX6U4ztMpcNpfbpWCV3JqUzBndj7_g"

    @Deprecated("Use com.projectronin.test.jwt.generateToken", replaceWith = ReplaceWith("generateToken", imports = ["com.projectronin.test.jwt.generateToken"]))
    fun generateToken(rsaKey: RSAKey, issuer: String, claimSetCustomizer: (JWTClaimsSet.Builder) -> JWTClaimsSet.Builder = { it }): String {
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.keyID).build(),
            claimSetCustomizer(
                JWTClaimsSet.Builder()
                    .subject("alice")
                    .issueTime(Date())
                    .issuer(issuer)
            )
                .build()
        )

        signedJWT.sign(RSASSASigner(rsaKey))
        return signedJWT.serialize()
    }

    @Deprecated("Seki no longer supported", replaceWith = ReplaceWith("generateToken", imports = ["com.projectronin.test.jwt.generateToken"]))
    fun generateSekiToken(secret: String = sekiSharedSecret, user: String = UUID.randomUUID().toString(), tenantId: String = "ejh3j95h"): String {
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.HS256).build(),
            JWTClaimsSet.Builder()
                .issueTime(Date())
                .subject(user)
                .issuer("Seki")
                .jwtID(UUID.randomUUID().toString())
                .claim("tenantid", tenantId)
                .build()
        )

        signedJWT.sign(MACSigner(secret))
        return signedJWT.serialize()
    }

    @Deprecated("Probably should have been private to start with", replaceWith = ReplaceWith("no replacement"))
    fun secretKey(key: String): SecretKey {
        return SecretKeySpec(key.toByteArray(), "HmacSHA256")
    }
}
