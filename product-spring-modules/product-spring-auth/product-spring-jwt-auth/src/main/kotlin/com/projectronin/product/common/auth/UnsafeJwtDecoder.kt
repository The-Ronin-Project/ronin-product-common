package com.projectronin.product.common.auth

import com.fasterxml.jackson.module.kotlin.readValue
import com.nimbusds.jose.JOSEObject
import com.projectronin.product.common.config.JsonProvider
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.time.Instant

/**
 * This class is used to decode a JWT **without making any verifications**, including signature verification.  It has two uses:
 *
 * - to decode a JWT for seki which will be sent to seki for verification (essentially treated as an opaque token)
 * - to peak at a JWT's claims without verification.  For example, if you need the issuer url to determine what auth manager to use,
 *   you would use this class to get that issuer without parsing it yourself.  Just be aware that no verification is performed,
 *   so it's not safe to use this to actually secure anything.
 */
class UnsafeJwtDecoder : JwtDecoder {
    override fun decode(token: String): Jwt {
        val parts = JOSEObject.split(token)

        val headers: Map<String, Any?> = JsonProvider.objectMapper.readValue(parts[0].decodeToString())
        val claims: Map<String, Any?> = JsonProvider.objectMapper.readValue<MutableMap<String, Any?>>(parts[1].decodeToString()).let { cl ->
            if (cl.contains(JwtClaimNames.IAT)) {
                cl[JwtClaimNames.IAT] = Instant.ofEpochSecond((cl[JwtClaimNames.IAT] as Number).toLong())
            }
            if (cl.contains(JwtClaimNames.EXP)) {
                cl[JwtClaimNames.EXP] = Instant.ofEpochSecond((cl[JwtClaimNames.EXP] as Number).toLong())
            }
            cl.toMap()
        }

        return Jwt.withTokenValue(token)
            .headers { h: MutableMap<String?, Any?> -> h.putAll(headers) }
            .claims { c: MutableMap<String?, Any?> -> c.putAll(claims) }
            .build()
    }
}
