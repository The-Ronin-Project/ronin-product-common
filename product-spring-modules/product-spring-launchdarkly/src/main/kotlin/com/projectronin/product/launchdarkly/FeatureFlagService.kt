package com.projectronin.product.launchdarkly

import com.launchdarkly.sdk.LDContext
import com.launchdarkly.sdk.server.LDClient
import jakarta.annotation.PreDestroy
import jakarta.validation.constraints.NotNull
import org.springframework.stereotype.Service

interface FeatureFlagService {
    fun <T> flag(flag: String, context: String, @NotNull default: T): T
}

@Service
class LaunchDarklyFeatureFlagService(
    private val client: LDClient
) : FeatureFlagService {

    fun contextFromKey(contextKey: String): LDContext = LDContext.builder(contextKey).build()

    @Suppress("UNCHECKED_CAST")
    override fun <T> flag(flag: String, context: String, @NotNull default: T): T {
        return when (default) {
            is Boolean ->
                client.boolVariation(flag, contextFromKey(context), default) as T
            is Int ->
                client.intVariation(flag, contextFromKey(context), default) as T
            is String ->
                client.stringVariation(flag, contextFromKey(context), default) as T
            else ->
                throw RuntimeException("Feature flag of type ${default!!::class.java.name} is not currently supported.")
        }
    }

    @PreDestroy
    fun shutdown() {
        client.close()
    }
}
