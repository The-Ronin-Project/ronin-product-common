package com.projectronin.product.contracttest.services

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
class ContractTestKafkaService(private vararg val topics: Topic) : ContractTestService {

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

                if (topics.isNotEmpty()) {
                    createTopics(topics)
                }

                _started = true
            }
        }
    }

    override fun stopSafely() {
        synchronized(this) {
            kotlin.runCatching {
                if (kafkaContainer.isRunning) {
                    kafkaContainer.stop()
                }
            }
                .onFailure { e -> logger.error("Kafka did not stop", e) }
        }
    }

    private fun createTopics(newTopics: Array<out Topic>) {
        val properties = Properties()
        properties["bootstrap.servers"] = kafkaContainer.bootstrapServers
        properties["client.id"] = "tc-admin-client"

        val admin = AdminClient.create(properties)
        admin.createTopics(newTopics.map { it.toNewTopic() })
    }
}

data class Topic(
    val name: String,
    val partitions: Int = 1,
    val replication: Int = 1
) {
    fun toNewTopic(): NewTopic {
        return NewTopic(name, partitions, replication.toShort())
    }
}
