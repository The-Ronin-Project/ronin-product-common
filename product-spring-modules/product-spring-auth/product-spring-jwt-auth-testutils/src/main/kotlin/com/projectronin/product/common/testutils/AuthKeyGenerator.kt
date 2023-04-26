package com.projectronin.product.common.testutils

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64

object AuthKeyGenerator {

    fun generateRandomRsa(): RSAKey {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        return RSAKey.Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyID(Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(keyPair.public.encoded)))
            .build()
    }

    fun createJWKS(key: RSAKey): JWKSet = JWKSet(key)

    fun createJWKSForPublicDisplay(jwkSet: JWKSet): String = ObjectMapper().writeValueAsString(jwkSet.toJSONObject(true))
}
