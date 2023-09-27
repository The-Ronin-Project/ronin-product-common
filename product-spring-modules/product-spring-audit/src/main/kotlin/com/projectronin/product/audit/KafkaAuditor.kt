package com.projectronin.product.audit

import com.projectronin.auth.RoninAuthentication
import com.projectronin.common.TenantId
import com.projectronin.kafka.data.RoninEvent
import com.projectronin.product.audit.config.AuditProperties
import com.projectronin.product.audit.messaging.v1.AuditCommandV1
import mu.KLogger
import mu.KotlinLogging
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.security.core.context.SecurityContextHolder
import java.time.OffsetDateTime

class KafkaAuditor(
    val producer: Producer<String, RoninEvent<AuditCommandV1>>?,
    private val auditProperties: AuditProperties
) : Auditor {
    private val logger: KLogger = KotlinLogging.logger { }

    override fun writeAudit(
        action: String,
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>?,
        mrn: String?
    ) {
        val auth = SecurityContextHolder.getContext().authentication as RoninAuthentication

        val entry = AuditCommandV1(
            auth.tenantId,
            auth.userId,
            auth.userFirstName,
            auth.userLastName,
            auth.userFullName,
            resourceCategory,
            resourceType,
            resourceId,
            mrn ?: "",
            action,
            OffsetDateTime.now(),
            dataMap?.map { (k, v) -> "$k:$v" }?.toList()
        )

        val wrapper = RoninEvent(
            tenantId = TenantId(auth.tenantId),
            source = auditProperties.sourceService,
            type = "AuditCommandV1",
            dataSchema =
            "https://github.com/projectronin/contract-messaging-audit/blob/main/v1/audit-command-v1.schema.json",
            data = entry
        )
        val record = ProducerRecord(auditProperties.topic, "$resourceType:$resourceId", wrapper)

        producer?.send(record) { recordMetadata, exception ->
            recordMetadata?.let {
                logger.info("Audit Entry written to Kafka: $recordMetadata")
            }
            exception?.let {
                logger.error("Audit Entry not written to Kafka: $exception")
            }
        }
        if (producer == null) {
            logger.info("Audit Entry not written since Kafka is disabled by ronin.kafka.disabled configuration")
        }
    }
}
