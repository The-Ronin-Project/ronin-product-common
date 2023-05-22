@file:Suppress("ktlint:no-wildcard-imports")

package com.projectronin.product.common.config

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class JwtSecurityPropertiesTest {

    @Test
    fun `should provide the correct match path patterns`() {
        val propsNull = JwtSecurityProperties()
        assertThat(propsNull.combinedMatchedPathPatterns()).containsExactlyInAnyOrder("/**")

        val propsOverride = JwtSecurityProperties(
            matchedPathPatterns = listOf("/foo/**", "/bar/**")
        )
        assertThat(propsOverride.combinedMatchedPathPatterns()).containsExactlyInAnyOrder("/foo/**", "/bar/**")

        val propsAdditional = JwtSecurityProperties(
            additionalMatchedPathPatterns = listOf("/foo/**", "/bar/**")
        )
        assertThat(propsAdditional.combinedMatchedPathPatterns()).containsExactlyInAnyOrder("/**", "/foo/**", "/bar/**")

        val propsAdditionalAndOverride = JwtSecurityProperties(
            matchedPathPatterns = listOf("/baz/**"),
            additionalMatchedPathPatterns = listOf("/foo/**", "/bar/**")
        )
        assertThat(propsAdditionalAndOverride.combinedMatchedPathPatterns()).containsExactlyInAnyOrder("/baz/**", "/foo/**", "/bar/**")
    }

    @Test
    fun `should provide the correct secured path patterns`() {
        val propsNull = JwtSecurityProperties()
        assertThat(propsNull.combinedSecuredPathPatterns()).containsExactlyInAnyOrder("/api/**")

        val propsOverride = JwtSecurityProperties(
            securedPathPatterns = listOf("/foo/**", "/bar/**")
        )
        assertThat(propsOverride.combinedSecuredPathPatterns()).containsExactlyInAnyOrder("/foo/**", "/bar/**")

        val propsAdditional = JwtSecurityProperties(
            additionalSecuredPathPatterns = listOf("/foo/**", "/bar/**")
        )
        assertThat(propsAdditional.combinedSecuredPathPatterns()).containsExactlyInAnyOrder("/api/**", "/foo/**", "/bar/**")

        val propsAdditionalAndOverride = JwtSecurityProperties(
            securedPathPatterns = listOf("/baz/**"),
            additionalSecuredPathPatterns = listOf("/foo/**", "/bar/**")
        )
        assertThat(propsAdditionalAndOverride.combinedSecuredPathPatterns()).containsExactlyInAnyOrder("/baz/**", "/foo/**", "/bar/**")
    }

    @Test
    fun `should provide the correct permitted path patterns`() {
        val propsNull = JwtSecurityProperties()
        assertThat(propsNull.combinedPermittedPathPatterns()).containsExactlyInAnyOrder("/actuator/**", "/swagger-ui/**", "/v3/api-docs/swagger-config", "/v*/*.json", "/error")

        val propsOverride = JwtSecurityProperties(
            permittedPathPatterns = listOf("/foo/**", "/bar/**")
        )
        assertThat(propsOverride.combinedPermittedPathPatterns()).containsExactlyInAnyOrder("/foo/**", "/bar/**")

        val propsAdditional = JwtSecurityProperties(
            additionalPermittedPathPatterns = listOf("/foo/**", "/bar/**")
        )
        assertThat(propsAdditional.combinedPermittedPathPatterns()).containsExactlyInAnyOrder("/actuator/**", "/swagger-ui/**", "/v3/api-docs/swagger-config", "/v*/*.json", "/error", "/foo/**", "/bar/**")

        val propsAdditionalAndOverride = JwtSecurityProperties(
            permittedPathPatterns = listOf("/baz/**"),
            additionalPermittedPathPatterns = listOf("/foo/**", "/bar/**")
        )
        assertThat(propsAdditionalAndOverride.combinedPermittedPathPatterns()).containsExactlyInAnyOrder("/baz/**", "/foo/**", "/bar/**")
    }
}
