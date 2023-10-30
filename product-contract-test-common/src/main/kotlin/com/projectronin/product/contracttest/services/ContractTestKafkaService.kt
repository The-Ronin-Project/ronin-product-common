package com.projectronin.product.contracttest.services

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

/**
 * Starts a Kafka service via docker. Provides the final host, port number, and the bootstrapServers connection string.
 */
class ContractTestKafkaService(val topics: List<Topic>) : ContractTestService {

    companion object {
        operator fun invoke(vararg topics: Topic) = ContractTestKafkaService(topics.toList())
    }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private var kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

    private var _started: Boolean = false

    val port: Int
        get() = kafkaContainer.getMappedPort(9093)

    val host: String
        get() = kafkaContainer.host

    val bootstrapServers: String
        get() = kafkaContainer.bootstrapServers

    override val started: Boolean
        get() = _started

    override val dependentServices: List<ContractTestService> = listOf()

    override val replacementTokens: Map<String, String>
        get() = mapOf(
            "bootstrapServers" to bootstrapServers,
            "kafkaPort" to port.toString(),
            "kafkaHost" to host
        )

    override fun start() {
        synchronized(this) {
            if (!_started) {
                logger.info("Starting kafka")
                kafkaContainer.start()

                createTopics()

                _started = true
            }
        }
    }

    override fun stopSafely() {
        synchronized(this) {
            runCatching {
                if (kafkaContainer.isRunning) {
                    kafkaContainer.stop()
                }
            }.onFailure { e -> logger.error("Kafka did not stop", e) }
        }
    }

    private fun createTopics() {
        val newTopics = topics.map { NewTopic(it.name, it.partitions, it.replication.toShort()) }
            .takeUnless { it.isEmpty() }
            ?: return

        withAdminClient { createTopics(newTopics) }
    }

    fun withAdminClient(block: AdminClient.() -> Unit) {
        createAdminClient().use { block(it) }
    }

    private fun createAdminClient() = AdminClient.create(
        Properties().apply {
            this[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = kafkaContainer.bootstrapServers
            this[CommonClientConfigs.CLIENT_ID_CONFIG] = "tc-admin-client"
        }
    )
}

data class Topic(
    val name: String,
    val partitions: Int = 1,
    val replication: Int = 1
)
