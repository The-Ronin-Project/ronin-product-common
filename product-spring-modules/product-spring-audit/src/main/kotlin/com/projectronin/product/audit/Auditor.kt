package com.projectronin.product.audit

import com.projectronin.kafka.data.RoninWrapper
import com.projectronin.product.audit.config.AuditProperties
import com.projectronin.product.audit.messaging.v1.AuditCommandV1
import com.projectronin.product.common.auth.RoninAuthentication
import mu.KLogger
import mu.KotlinLogging
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.security.core.context.SecurityContextHolder
import java.time.OffsetDateTime

class Auditor(
    val producer: Producer<String, RoninWrapper<AuditCommandV1>>,
    val auditProperties: AuditProperties
) {
    private val logger: KLogger = KotlinLogging.logger { }

    fun read(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    ) {
        writeAudit("READ", resourceCategory, resourceType, resourceId, dataMap, mrn)
    }

    fun create(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    ) {
        writeAudit("CREATE", resourceCategory, resourceType, resourceId, dataMap, mrn)
    }

    fun update(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    ) {
        writeAudit("UPDATE", resourceCategory, resourceType, resourceId, dataMap, mrn)
    }

    fun delete(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    ) {
        writeAudit("DELETE", resourceCategory, resourceType, resourceId, dataMap, mrn)
    }

    @Suppress("LongParameterList")
    private fun writeAudit(
        action: String,
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
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

        val wrapper = RoninWrapper(
            tenantId = auth.tenantId,
            sourceService = auditProperties.sourceService,
            dataType = "AuditCommandV1",
            data = entry
        )
        val record = ProducerRecord(auditProperties.topic, "$resourceType:$resourceId", wrapper)
        val recordMetadata = producer.send(record).get()
        logger.info("Audit Entry written to Kafka: $recordMetadata")
    }
}
