package com.projectronin.product.common.tenant.stream.config

import com.projectronin.common.TenantId
import com.projectronin.json.tenant.v1.TenantV1Schema
import com.projectronin.tenant.config.TenantStreamConfig
import com.projectronin.tenant.handlers.TenantEventHandler
import com.projectronin.tenant.stream.TenantEventStream
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class TenantStreamConfigurationTest {
    @Test
    fun `Tenant stream is created when enabled`() {
        val context = ApplicationContextRunner()
            .withBean(LoggingMeterRegistry::class.java)
            .withBean(TestEventHandler::class.java)
            .withPropertyValues(
                "ronin.kafka.bootstrapServers=localhost:9092",
                "ronin.kafka.security-protocol=PLAINTEXT",
                "ronin.product.tenant.application-id=test.tenant.v1",
                "ronin.product.tenant.dlq-topic=test.dlq"
            )
            .withUserConfiguration(TenantStreamConfiguration::class.java)

        context.run {
            Assertions.assertThat(it).hasSingleBean(TenantStreamConfig::class.java)
            Assertions.assertThat(it).hasSingleBean(TenantEventStream::class.java)
        }
    }

    @Test
    fun `Tenant stream is not created when disabled`() {
        val context = ApplicationContextRunner()
            .withBean(LoggingMeterRegistry::class.java)
            .withBean(TestEventHandler::class.java)
            .withPropertyValues(
                "ronin.kafka.bootstrapServers=localhost:9092",
                "ronin.kafka.security-protocol=PLAINTEXT"
            )

        context.run {
            Assertions.assertThat(it).doesNotHaveBean(TenantStreamConfig::class.java)
            Assertions.assertThat(it).doesNotHaveBean(TenantEventStream::class.java)
        }
    }
}

class TestEventHandler : TenantEventHandler {
    override fun create(data: TenantV1Schema) {}

    override fun update(data: TenantV1Schema) {}

    override fun delete(tenantId: TenantId) {}
}
