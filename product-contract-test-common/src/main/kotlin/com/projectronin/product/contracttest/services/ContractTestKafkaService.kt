package com.projectronin.product.contracttest.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

/**
 * Starts a Kafka service via docker. Provides the final host, port number, and the bootstrapServers connection string.
 */
class ContractTestKafkaService() : ContractTestService {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private var kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

    private var _started: Boolean = false

    val kafkaPort: Int
        get() = kafkaContainer.getMappedPort(9093)

    override val started: Boolean
        get() = _started

    override val dependentServices: List<ContractTestService> = listOf()

    override val replacementTokens: Map<String, String>
        get() = mapOf(
            "bootstrapServers" to kafkaContainer.bootstrapServers,
            "kafkaPort" to kafkaPort.toString(),
            "kafkaHost" to kafkaContainer.host
        )

    override fun start() {
        synchronized(this) {
            if (!_started) {
                logger.info("Starting kafka")
                kafkaContainer.start()
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
}
