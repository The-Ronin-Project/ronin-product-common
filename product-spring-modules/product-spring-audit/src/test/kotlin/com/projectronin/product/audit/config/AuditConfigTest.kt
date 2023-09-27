package com.projectronin.product.audit.config

import com.projectronin.kafka.data.RoninEvent
import com.projectronin.product.audit.Auditor
import com.projectronin.product.audit.KafkaAuditor
import com.projectronin.product.audit.LogAuditor
import com.projectronin.product.audit.messaging.v1.AuditCommandV1
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import org.apache.kafka.clients.producer.MockProducer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

class AuditConfigTest {

    @Test
    fun `test Auditor is created without producer when not enabled`() {
        val context = ApplicationContextRunner()
            .withBean(LoggingMeterRegistry::class.java)
            .withPropertyValues(
                "ronin.kafka.bootstrapServers=localhost",
                "ronin.kafka.security-protocol=PLAINTEXT",
                "ronin.product.audit.sourceService=testSourceService",
                "ronin.product.audit.enabled=false"
            )
            .withUserConfiguration(AuditConfig::class.java)

        context.run {
            assertThat(it).hasSingleBean(Auditor::class.java)
            val auditor = it.getBean(Auditor::class.java)
            assertThat(auditor is LogAuditor).isEqualTo(true)
            auditor.create("category", "type", "id", null, null)
            auditor.read("category", "type", "id", null, null)
            auditor.update("category", "type", "id", null, null)
            auditor.delete("category", "type", "id", null, null)
        }
    }

    @Test
    fun `test Auditor is created with producer when enabled excluded`() {
        val context = ApplicationContextRunner()
            .withUserConfiguration(testConfig::class.java)
            .withBean(LoggingMeterRegistry::class.java)
            .withPropertyValues(
                "ronin.kafka.bootstrapServers=localhost:9092",
                "ronin.kafka.security-protocol=PLAINTEXT",
                "ronin.product.audit.sourceService=testSourceService"
            )
            .withUserConfiguration(AuditConfig::class.java)

        context.run {
            assertThat(it).hasSingleBean(Auditor::class.java)
            val auditor = it.getBean(Auditor::class.java)
            assertThat(auditor is KafkaAuditor).isEqualTo(true)
        }
    }

    class testConfig {
        @Bean
        fun producer(): MockProducer<String, RoninEvent<AuditCommandV1>> {
            return MockProducer<String, RoninEvent<AuditCommandV1>>()
        }
    }
}
