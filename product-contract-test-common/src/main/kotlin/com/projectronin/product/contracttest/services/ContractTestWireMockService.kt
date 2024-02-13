package com.projectronin.product.contracttest.services

import com.github.tomakehurst.wiremock.client.WireMock
import com.projectronin.domaintest.DomainTestSetupContext
import com.projectronin.domaintest.externalWiremockPort

/**
 * Starts an embedded wiremock server.  Doesn't use docker to do so.  Provides the wiremock port, and also configures the static WireMock class.
 */
class ContractTestWireMockService : ContractTestService {

    val wireMockPort: Int
        get() = externalWiremockPort

    override val started: Boolean
        get() = true

    override val dependentServices: List<ContractTestService> = listOf()

    override val replacementTokens: Map<String, String>
        get() = mapOf("wireMockPort" to "8080")

    override val internalReplacementTokens: Map<String, String>
        get() = replacementTokens

    fun reset() {
        WireMock.resetToDefault()
    }

    override fun setupAgainstDomainTest(): DomainTestSetupContext.() -> Unit = {
        withWireMock()
    }
}
