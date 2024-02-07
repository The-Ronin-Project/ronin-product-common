package com.projectronin.product.common.tenant.stream.config

import com.projectronin.kafka.config.ClusterProperties
import com.projectronin.kafka.spring.config.KafkaConfiguration
import com.projectronin.tenant.config.TenantStreamConfig
import com.projectronin.tenant.handlers.TenantEventHandler
import com.projectronin.tenant.stream.TenantEventStream
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@EnableConfigurationProperties(TenantStreamProperties::class)
@Import(KafkaConfiguration::class)
open class TenantStreamConfiguration {
    @Bean
    fun tenantStreamConfig(
        clusterProperties: ClusterProperties,
        tenantStreamProperties: TenantStreamProperties,
        tenantEventHandler: TenantEventHandler
    ): TenantStreamConfig {
        return TenantStreamConfig(
            clusterProperties = clusterProperties,
            applicationId = tenantStreamProperties.applicationId,
            tenantTopic = tenantStreamProperties.tenantTopic,
            dlqTopic = tenantStreamProperties.dlqTopic,
            handler = tenantEventHandler
        )
    }

    @Bean
    fun tenantEventStream(
        tenantStreamConfig: TenantStreamConfig
    ): TenantEventStream {
        return TenantEventStream(tenantStreamConfig).also { it.initialize() }
    }
}
