package com.projectronin.product.contracttest

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.domaintest.DomainTestExtension
import com.projectronin.product.contracttest.services.ContractTestService
import okhttp3.OkHttpClient
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.TimeUnit

/**
 * A JUnit Jupiter extension that starts a set of services up before the test suite and shuts them down after
 * the test suite.  It scans the classpath for implementations of `ContractServicesProvider` and retrieves
 * a list of services from them, and then starts up all the services.  When the suite ends, it shuts down
 * those services.h
 *
 * To make this work, you need to provide an implementation of `ContractServicesProvider`.
 */
@Deprecated("Please migrate to ronincommon.local.contract.test / com.projectronin:local-contract-test")
class LocalContractTestExtension : BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    companion object {
        private val delegate = DomainTestExtension()

        private val services: LinkedHashSet<ContractTestService> = linkedSetOf()

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(60L, TimeUnit.SECONDS)
            .readTimeout(60L, TimeUnit.SECONDS)
            .build()

        // object mapper for convenience of reading and writing requests and responses.
        val objectMapper: ObjectMapper by lazy {
            val m = ObjectMapper()
            m.findAndRegisterModules()
            m
        }

        /**
         * Searches the list of running services for the first service that matches the given type.  This can be used to get the running instance of the service
         * in order to retrieve (for instance) a dynamically allocated port number, or a password, etc.
         */
        inline fun <reified T : ContractTestService> serviceOfType(): T? {
            return allServices().find { it is T } as T?
        }

        fun allServices(): Set<ContractTestService> = services

        internal fun addServices(services: Iterable<ContractTestService>) {
            this.services += services
        }
    }

    override fun beforeAll(context: ExtensionContext) {
        delegate.beforeAll(context)
    }

    override fun close() {
        delegate.close()
    }
}
