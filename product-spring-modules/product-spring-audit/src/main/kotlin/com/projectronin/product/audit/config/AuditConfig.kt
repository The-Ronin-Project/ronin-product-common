package com.projectronin.product.audit.config

import com.projectronin.kafka.data.RoninEvent
import com.projectronin.kafka.spring.config.KafkaConfiguration
import com.projectronin.kafka.spring.config.ProducerConfiguration
import com.projectronin.product.audit.Auditor
import com.projectronin.product.audit.KafkaAuditor
import com.projectronin.product.audit.LogAuditor
import com.projectronin.product.audit.messaging.v1.AuditCommandV1
import com.projectronin.product.common.base.ModulePropertySourceFactory
import org.apache.kafka.clients.producer.Producer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

@AutoConfiguration
@ConditionalOnProperty(name = ["ronin.product.audit"], matchIfMissing = true)
@PropertySource(
    name = "Audit Configuration",
    value = ["classpath:audit-application.yml"],
    factory = ModulePropertySourceFactory::class
)
@EnableConfigurationProperties(AuditProperties::class)
@Import(KafkaConfiguration::class, ProducerConfiguration::class)
open class AuditConfig {
    @Bean
    @ConditionalOnProperty(
        prefix = "ronin.product.audit",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    open fun kafkaAuditor(
        auditProperties: AuditProperties,
        producer: Producer<String, RoninEvent<AuditCommandV1>>
    ): Auditor {
        return KafkaAuditor(producer, auditProperties)
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "ronin.product.audit",
        name = ["enabled"],
        havingValue = "false",
        matchIfMissing = false
    )
    open fun logAuditor(auditProperties: AuditProperties): Auditor {
        return LogAuditor(auditProperties)
    }
}
