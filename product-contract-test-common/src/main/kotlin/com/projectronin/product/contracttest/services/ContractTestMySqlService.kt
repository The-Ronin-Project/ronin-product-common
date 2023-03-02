package com.projectronin.product.contracttest.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.MySQLContainer

/**
 * Starts a MySQL service via docker.  Uses the given username, password, and database.  Provides the final port number
 * and a constructed JDBC connection URI.
 */
class ContractTestMySqlService(val dbName: String, val username: String, val password: String) : ContractTestService {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private var mySqlContainer = MySQLContainer("mysql:8")
        .withDatabaseName(dbName)
        .withUsername(username)
        .withPassword(password)

    private var _started: Boolean = false

    val mySqlPort: Int
        get() = mySqlContainer.getMappedPort(3306)

    override val started: Boolean
        get() = _started

    override val dependentServices: List<ContractTestService> = listOf()

    override val replacementTokens: Map<String, String>
        get() = mapOf(
            "mySqlPort" to mySqlPort.toString(),
            "mySqlJdbcUri" to "jdbc:mysql://$username:$password@localhost:$mySqlPort/$dbName?createDatabaseIfNotExist=true",
        )

    override fun start() {
        synchronized(this) {
            if (!_started) {
                logger.info("Starting mysql")
                mySqlContainer.start()
                _started = true
            }
        }
    }

    override fun stopSafely() {
        synchronized(this) {
            kotlin.runCatching {
                if (mySqlContainer.isRunning) {
                    mySqlContainer.stop()
                }
            }
                .onFailure { e -> logger.error("MySQL did not stop", e) }
        }
    }
}
