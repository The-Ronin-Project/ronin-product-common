package com.projectronin.product.contracttest.services

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Starts an embedded wiremock server.  Doesn't use docker to do so.  Provides the wiremock port, and also configures the static WireMock class.
 */
class ContractTestWireMockService : ContractTestService {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

    private var _started: Boolean = false

    val wireMockPort: Int
        get() = wireMockServer.port()

    override val started: Boolean
        get() = _started

    override val dependentServices: List<ContractTestService> = listOf()

    override val replacementTokens: Map<String, String>
        get() = mapOf("wireMockPort" to wireMockPort.toString())

    override fun start() {
        synchronized(this) {
            if (!_started) {
                logger.info("Starting wiremock")
                wireMockServer.start()
                WireMock.configureFor(wireMockPort)
                _started = true
            }
        }
    }

    override fun stopSafely() {
        synchronized(this) {
            kotlin.runCatching {
                if (wireMockServer.isRunning) {
                    wireMockServer.stop()
                }
            }
                .onFailure { e -> logger.error("WireMock did not stop", e) }
        }
    }

    fun reset() {
        WireMock.resetToDefault()
    }
}
