package com.projectronin.product.audit

import com.projectronin.auth.RoninAuthentication
import com.projectronin.common.TenantId
import com.projectronin.kafka.data.RoninEvent
import com.projectronin.product.audit.ReactiveKafkaAuditor.AuditOperator
import com.projectronin.product.audit.config.AuditProperties
import com.projectronin.product.audit.messaging.v1.AuditCommandV1
import mu.KLogger
import mu.KotlinLogging
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.security.core.context.SecurityContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import java.time.OffsetDateTime
import java.util.function.Consumer

enum class AuditOperation {
    READ,
    CREATE,
    UPDATE,
    DELETE;
}
class ReactiveKafkaAuditor(
    val producer: Producer<String, RoninEvent<AuditCommandV1>>?,
    private val auditProperties: AuditProperties
) {
    private val logger: KLogger = KotlinLogging.logger { }

    class AuditParams(
        var resourceCategory: String = "",
        var resourceType: String = "",
        var resourceId: String = "",
        var dataMap: Map<String, Any>? = null,
        var mrn: String? = null
    )

    @FunctionalInterface
    private fun interface AuditOperator {
        fun perform(mapper: AuditParams, auth: RoninAuthentication)
    }

    private fun getOperator(operation: AuditOperation): AuditOperator = when (operation) {
        AuditOperation.CREATE -> AuditOperator { ap: AuditParams, auth: RoninAuthentication ->
            writeAudit("CREATE", ap.resourceCategory, ap.resourceType, ap.resourceId, ap.dataMap, ap.mrn, auth)
        }

        AuditOperation.READ -> AuditOperator { ap: AuditParams, auth: RoninAuthentication ->
            writeAudit("READ", ap.resourceCategory, ap.resourceType, ap.resourceId, ap.dataMap, ap.mrn, auth)
        }

        AuditOperation.UPDATE -> AuditOperator { ap: AuditParams, auth: RoninAuthentication ->
            writeAudit("UPDATE", ap.resourceCategory, ap.resourceType, ap.resourceId, ap.dataMap, ap.mrn, auth)
        }

        AuditOperation.DELETE -> AuditOperator { ap: AuditParams, auth: RoninAuthentication ->
            writeAudit("DELETE", ap.resourceCategory, ap.resourceType, ap.resourceId, ap.dataMap, ap.mrn, auth)
        }
    }

    private fun writeAudit(
        action: String,
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>?,
        mrn: String?,
        auth: RoninAuthentication
    ) {
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
            dataSchema = "https://github.com/projectronin/contract-messaging-audit/blob/main/v1/audit-command-v1.schema.json",
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

    companion object {
        private fun <T> Mono<T>.audit(
            auditor: ReactiveKafkaAuditor,
            operation: AuditOperation,
            block: AuditParams.(T) -> Unit
        ) = doOnEach(consumer(auditor.getOperator(operation), block))

        fun <T> Mono<T>.auditCreate(auditor: ReactiveKafkaAuditor, block: AuditParams.(T) -> Unit) =
            audit(auditor, AuditOperation.CREATE, block)

        fun <T> Mono<T>.auditRead(auditor: ReactiveKafkaAuditor, block: AuditParams.(T) -> Unit) =
            audit(auditor, AuditOperation.READ, block)

        fun <T> Mono<T>.auditUpdate(auditor: ReactiveKafkaAuditor, block: AuditParams.(T) -> Unit) =
            audit(auditor, AuditOperation.UPDATE, block)

        fun <T> Mono<T>.auditDelete(auditor: ReactiveKafkaAuditor, block: AuditParams.(T) -> Unit) =
            audit(auditor, AuditOperation.DELETE, block)

        private fun <T> Flux<T>.audit(
            auditor: ReactiveKafkaAuditor,
            operation: AuditOperation,
            block: AuditParams.(T) -> Unit
        ) = doOnEach(consumer(auditor.getOperator(operation), block))

        fun <T> Flux<T>.auditCreate(auditor: ReactiveKafkaAuditor, block: AuditParams.(T) -> Unit) =
            audit(auditor, AuditOperation.CREATE, block)

        fun <T> Flux<T>.auditRead(auditor: ReactiveKafkaAuditor, block: AuditParams.(T) -> Unit) =
            audit(auditor, AuditOperation.READ, block)

        fun <T> Flux<T>.auditUpdate(auditor: ReactiveKafkaAuditor, block: AuditParams.(T) -> Unit) =
            audit(auditor, AuditOperation.UPDATE, block)

        fun <T> Flux<T>.auditDelete(auditor: ReactiveKafkaAuditor, block: AuditParams.(T) -> Unit) =
            audit(auditor, AuditOperation.DELETE, block)

        private fun <T> consumer(operator: AuditOperator, block: AuditParams.(T) -> Unit) =
            Consumer<Signal<T>> { signal ->
                if (signal.isOnNext && signal.hasValue()) {
                    signal.contextView
                        .getOrEmpty<Mono<SecurityContext>>(SecurityContext::class.java)
                        .ifPresentOrElse({ stream ->
                            stream.doOnSuccess {
                                val authentication = it.authentication as? RoninAuthentication
                                    ?: throw ReactiveAuditorContextException("Authentication was not a valid type.")

                                operator.perform(
                                    AuditParams().apply { block(signal.get()!!) },
                                    authentication
                                )
                            }.doOnError {
                                throw ReactiveAuditorContextException("Failed to retrieve authentication from the reactive context.")
                            }.subscribe()
                        }, {
                            throw ReactiveAuditorContextException("Authentication was not present in reactive context.")
                        })
                }
            }
    }
}

class ReactiveAuditorContextException(message: String) : RuntimeException(message)
