package com.projectronin.product.audit.config

import com.projectronin.kafka.config.KafkaConfigurator
import com.projectronin.kafka.config.KafkaConfigurator.ClientType.PRODUCER
import com.projectronin.kafka.data.RoninWrapper
import com.projectronin.kafka.handlers.LogAndContinueProductionExceptionHandler
import com.projectronin.kafka.serde.wrapper.WrapperSerializer
import com.projectronin.product.audit.Auditor
import com.projectronin.product.audit.messaging.v1.AuditCommandV1
import com.projectronin.product.common.kafka.config.KafkaClusterProperties
import org.apache.kafka.clients.producer.KafkaProducer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnProperty(name = ["ronin.product.audit"], matchIfMissing = true)
@EnableConfigurationProperties(AuditProperties::class)
open class AuditConfig {

    @Bean
    open fun auditor(
        clusterProperties: KafkaClusterProperties,
        auditProperties: AuditProperties
    ): Auditor {
        val kafkaProducer = KafkaProducer<String, RoninWrapper<AuditCommandV1>>(
            KafkaConfigurator.builder(PRODUCER, clusterProperties)
                .withValueSerializer(WrapperSerializer::class)
                .withProductionExceptionHandler(LogAndContinueProductionExceptionHandler::class)
                .configs()
        )

        return Auditor(kafkaProducer, auditProperties)
    }
}
