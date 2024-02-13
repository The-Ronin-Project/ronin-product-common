package com.projectronin.product.contracttest.services

import com.projectronin.domaintest.DomainTestSetupContext
import com.projectronin.domaintest.SupportingServices
import com.projectronin.domaintest.kafkaExternalBootstrapServers
import com.projectronin.domaintest.kafkaExternalHost
import com.projectronin.domaintest.kafkaInternalBootstrapServers
import com.projectronin.domaintest.kafkaPort
import com.projectronin.domaintest.withKafkaAdminClient
import org.apache.kafka.clients.admin.AdminClient

/**
 * Starts a Kafka service via docker. Provides the final host, port number, and the bootstrapServers connection string.
 */
class ContractTestKafkaService(val topics: List<Topic>) : ContractTestService {

    companion object {
        operator fun invoke(vararg topics: Topic) = ContractTestKafkaService(topics.toList())
    }

    val port: Int
        get() = kafkaPort

    val host: String
        get() = kafkaExternalHost

    val bootstrapServers: String
        get() = kafkaExternalBootstrapServers

    override val started: Boolean
        get() = true

    override val dependentServices: List<ContractTestService> = listOf()

    override val replacementTokens: Map<String, String>
        get() = mapOf(
            "bootstrapServers" to kafkaInternalBootstrapServers,
            "kafkaPort" to "9093",
            "kafkaHost" to SupportingServices.Kafka.containerName
        )

    override val internalReplacementTokens: Map<String, String>
        get() = replacementTokens

    override fun setupAgainstDomainTest(): DomainTestSetupContext.() -> Unit = {
        withKafka {
            topics.forEach {
                topic(it.name, it.partitions, it.replication)
            }
        }
    }

    fun withAdminClient(block: AdminClient.() -> Unit) {
        withKafkaAdminClient(block)
    }
}

data class Topic(
    val name: String,
    val partitions: Int = 1,
    val replication: Int = 1
)
