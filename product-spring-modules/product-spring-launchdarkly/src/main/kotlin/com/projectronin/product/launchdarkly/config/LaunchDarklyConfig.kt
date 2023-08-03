package com.projectronin.product.launchdarkly.config

import com.launchdarkly.sdk.server.Components
import com.launchdarkly.sdk.server.LDClient
import com.launchdarkly.sdk.server.LDConfig
import com.launchdarkly.sdk.server.integrations.TestData
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@AutoConfiguration
@EnableConfigurationProperties(LaunchDarklyProperties::class)
open class LaunchDarklyConfig {
    @Bean
    @ConditionalOnProperty("ronin.product.launch-darkly.client-sdk-key", havingValue = "false", matchIfMissing = true)
    open fun testData(): TestData = TestData.dataSource()

    @Bean
    @ConditionalOnProperty("ronin.product.launch-darkly.client-sdk-key", havingValue = "false", matchIfMissing = true)
    open fun testLaunchDarklyClient(testData: TestData): LDClient {
        val config = LDConfig.Builder().run {
            diagnosticOptOut(true)
            events(Components.noEvents())
            dataSource(testData)
            build()
        }
        return LDClient("dummy-test-sdk-key", config)
    }

    @Bean
    @ConditionalOnProperty("ronin.product.launch-darkly.client-sdk-key")
    @Primary
    open fun launchDarklyClient(ldProps: LaunchDarklyProperties): LDClient {
        val config = LDConfig.Builder().run {
            offline(ldProps.offline)
            diagnosticOptOut(ldProps.diagnosticOptOut)
            build()
        }
        return LDClient(ldProps.clientSdkKey, config)
    }
}
