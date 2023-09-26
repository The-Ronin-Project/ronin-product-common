package com.projectronin.product.audit.config

import com.projectronin.kafka.config.ClusterProperties
import com.projectronin.kafka.config.ProducerProperties
import com.projectronin.kafka.data.RoninEvent
import com.projectronin.kafka.spring.config.KafkaConfiguration
import com.projectronin.kafka.spring.config.ProducerConfiguration
import com.projectronin.product.audit.Auditor
import com.projectronin.product.audit.messaging.v1.AuditCommandV1
import com.projectronin.product.common.base.ModulePropertySourceFactory
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.producer.Producer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import java.util.*

@AutoConfiguration
@ConditionalOnProperty(name = ["ronin.product.audit"], matchIfMissing = true)
@PropertySource(
    name = "Audit Configuration",
    value = ["classpath:audit-application.yml"],
    factory = ModulePropertySourceFactory::class
)
@EnableConfigurationProperties(AuditProperties::class)
@Import(KafkaConfiguration::class)
open class AuditConfig {
    private val producerConfiguration = ProducerConfiguration()

    @Bean
    open fun producerProperties(clusterProperties: ClusterProperties): ProducerProperties {
        return producerConfiguration.defaultProducerProperties(clusterProperties)
    }

    @Bean(name = ["auditorProducer"], destroyMethod = "flush")
    @ConditionalOnProperty(
        prefix = "ronin.product.audit",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    open fun <T> kafkaProducer(
        producerProperties: ProducerProperties,
        meterRegistry: Optional<MeterRegistry>
    ): Producer<String, T> {
        return producerConfiguration.kafkaProducer(producerProperties, meterRegistry)
    }

    @Bean
    open fun auditor(
        auditProperties: AuditProperties,
        @Qualifier("auditorProducer")
        producer: Producer<String, RoninEvent<AuditCommandV1>>?
    ): Auditor {
        return Auditor(producer, auditProperties)
    }
}
