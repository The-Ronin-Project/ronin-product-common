package com.projectronin.product.audit.config

import com.projectronin.product.common.kafka.config.KafkaClusterProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuditConfigTest {

    @Test
    fun `Test disabled uses null producer`() {
        val config = AuditConfig()
        val auditor = config.auditor(
            KafkaClusterProperties(bootstrapServers = "test", disabled = true),
            AuditProperties("command", "source")
        )

        assertThat(auditor.producer).isNull()
    }
}
