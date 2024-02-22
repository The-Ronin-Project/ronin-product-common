package com.projectronin.product.common.auth

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

const val sekiSharedSecret = "23jB5lMDPhXXahTBosjuUFhoMK0joALW0tQDa5ydqS5QyoPcA8tev4BVsoZltej5"

fun secretKey(key: String): SecretKey {
    return SecretKeySpec(key.toByteArray(), "HmacSHA256")
}

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
