package com.projectronin.product.contracttest.services

import com.projectronin.domaintest.DomainTestSetupContext
import com.projectronin.domaintest.SupportingServices
import com.projectronin.domaintest.externalJdbcUrlFor
import com.projectronin.domaintest.externalMySqlPort
import java.sql.Connection
import java.sql.DriverManager

/**
 * Starts a MySQL service via docker.  Uses the given username, password, and database.  Provides the final port number
 * and a constructed JDBC connection URI.
 */
class ContractTestMySqlService(val dbName: String, val username: String, val password: String) : ContractTestService {

    val mySqlPort: Int
        get() = externalMySqlPort

    val jdbcUri: String
        get() = externalJdbcUrlFor(dbName)

    override val started: Boolean
        get() = true

    override val dependentServices: List<ContractTestService> = listOf()

    override val replacementTokens: Map<String, String>
        get() = mapOf(
            "mySqlPort" to mySqlPort.toString(),
            "mySqlJdbcUri" to jdbcUri,
            "mySqlR2dbcUri" to "r2dbc:mysql://$username:$password@localhost:$mySqlPort/$dbName?createDatabaseIfNotExist=true"
        )

    override val internalReplacementTokens: Map<String, String>
        get() = mapOf(
            "mySqlPort" to "3306",
            "mySqlJdbcUri" to "jdbc:mysql://$username:$password@${SupportingServices.MySql.containerName}:3306/$dbName?createDatabaseIfNotExist=true",
            "mySqlR2dbcUri" to "r2dbc:mysql://$username:$password@${SupportingServices.MySql.containerName}:3306/$dbName?createDatabaseIfNotExist=true"
        )

    override fun setupAgainstDomainTest(): DomainTestSetupContext.() -> Unit = {
        withMySQL {
            withDatabase(dbName, username, password)
        }
    }

    fun createConnection(): Connection = DriverManager.getConnection(externalJdbcUrlFor(dbName))
}
