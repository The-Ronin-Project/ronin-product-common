package com.projectronin.product.common.testutils

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64

@Deprecated("Use functions in ronin-common:jwt-auth-test")
object AuthKeyGenerator {

    @Deprecated("Use com.projectronin.test.jwt.generateRandomRsa", replaceWith = ReplaceWith("generateRandomRsa", imports = ["com.projectronin.test.jwt.generateRandomRsa"]))
    fun generateRandomRsa(): RSAKey {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        return RSAKey.Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyID(Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(keyPair.public.encoded)))
            .build()
    }

    @Deprecated("Use com.projectronin.test.jwt.createJWKS", replaceWith = ReplaceWith("createJWKS", imports = ["com.projectronin.test.jwt.createJWKS"]))
    fun createJWKS(key: RSAKey): JWKSet = JWKSet(key)

    @Deprecated("Use com.projectronin.test.jwt.createJWKSForPublicDisplay", replaceWith = ReplaceWith("createJWKSForPublicDisplay", imports = ["com.projectronin.test.jwt.createJWKSForPublicDisplay"]))
    fun createJWKSForPublicDisplay(jwkSet: JWKSet): String = ObjectMapper().writeValueAsString(jwkSet.toJSONObject(true))
}
