package com.projectronin.product.audit.config

import com.projectronin.product.audit.Auditor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class AuditConfigTest {

    @Test
    fun `test Auditor is created without producer when not enabled`() {
        val context = ApplicationContextRunner()
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
            assertThat(auditor.producer).isNull()
        }
    }

    @Test
    fun `test Auditor is created with producer when enabled excluded`() {
        val context = ApplicationContextRunner()
            .withPropertyValues(
                "ronin.kafka.bootstrapServers=localhost:9092",
                "ronin.kafka.security-protocol=PLAINTEXT",
                "ronin.product.audit.sourceService=testSourceService"
            )
            .withUserConfiguration(AuditConfig::class.java)

        context.run {
            assertThat(it).hasSingleBean(Auditor::class.java)
            val auditor = it.getBean(Auditor::class.java)
            assertThat(auditor.producer).isNotNull()
        }
    }
}
