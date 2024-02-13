package com.projectronin.product.contracttest.services

import com.projectronin.domaintest.DomainTestSetupContext

/**
 * Represents a startable / stoppable service for contract testing.  Examples might be a docker container (MySQL, Kafka),
 * an embedded process (wiremock), or an externally launched service (a spring boot app you're trying to test).  Via a `ContractServicesProvider`,
 * you provide a list of these to the LocalContractTestExtension, and the extension starts and stops them all.
 */
interface ContractTestService {

    /**
     * Was it started?
     */
    val started: Boolean

    /**
     * A list of dependent services.  May not be null, but may be empty.  These will be started
     * before this service is started.
     */
    val dependentServices: List<ContractTestService>

    /**
     * A list of tokens to be replaced in the application-test.properties file (or, any file).  In the current implementation,
     * these are iterated and instances of `{{key}}` are replaced by the value of each entry.
     */
    val replacementTokens: Map<String, String>

    /**
     * A list of tokens to be replaced in the application-test.properties file (or, any file).  In the current implementation,
     * these are iterated and instances of `{{key}}` are replaced by the value of each entry.
     */
    val internalReplacementTokens: Map<String, String>

    /**
     * Start the service.  Implementations should be idempotent and thread-safe.
     */
    fun start() {
        throw IllegalStateException("No longer used")
    }

    /**
     * Stops the service.  Implementations should be idempotent and thread-safe and should avoid throwing exceptions.
     */
    fun stopSafely() {
        throw IllegalStateException("No longer used")
    }

    /**
     * Adapts this whole thing to the domain test framework.  This one is the only one that's really called now.
     */
    fun setupAgainstDomainTest(): DomainTestSetupContext.() -> Unit
}
