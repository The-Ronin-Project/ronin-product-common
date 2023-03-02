package com.projectronin.product.contracttest

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.contracttest.services.ContractServicesProvider
import com.projectronin.product.contracttest.services.ContractTestService
import okhttp3.OkHttpClient
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.fail
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * A JUnit Jupiter extension that starts a set of services up before the test suite and shuts them down after
 * the test suite.  It scans the classpath for implementations of `ContractServicesProvider` and retrieves
 * a list of services from them, and then starts up all the services.  When the suite ends, it shuts down
 * those services.h
 *
 * To make this work, you need to provide an implementation of `ContractServicesProvider`.
 */
class LocalContractTestExtension : BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    companion object {
        private val uuid: UUID = UUID.randomUUID()

        private val started: Boolean by lazy {
            start()
            true
        }

        // the http client you will use to talk to your service
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

        private val logger: Logger = LoggerFactory.getLogger(LocalContractTestExtension::class.java)

        private var services: LinkedHashSet<ContractTestService> = linkedSetOf()

        private fun start() {
            val provider = ClassPathScanningCandidateComponentProvider(false)
            provider.addIncludeFilter(AssignableTypeFilter(ContractServicesProvider::class.java))
            provider.findCandidateComponents("com.projectronin")
                .filter { !it.isAbstract }
                .forEach { bd ->
                    addWithDependencies((Class.forName(bd.beanClassName).getConstructor().newInstance() as ContractServicesProvider).provideServices())
                }
            kotlin.runCatching {
                services.forEach { it.start() }
            }
                .onFailure {
                    logger.error("Error starting up: ", it)
                    services.forEach { s -> s.stopSafely() }
                    throw it
                }
        }

        private fun stop() {
            services.forEach { s -> s.stopSafely() }
        }

        private fun addWithDependencies(services: List<ContractTestService>) {
            services.forEach(::addWithDependencies)
        }

        private fun addWithDependencies(service: ContractTestService) {
            if (!services.contains(service)) {
                addWithDependencies(service.dependentServices)
                services += service
            }
        }

        /**
         * Searches the list of running services for the first service that matches the given type.  This can be used to get the running instance of the service
         * in order to retrieve (for instance) a dynamically allocated port number, or a password, etc.
         */
        inline fun <reified T : ContractTestService> serviceOfType(): T? {
            return allServices().find { it is T } as T?
        }

        fun allServices(): Set<ContractTestService> = services
    }

    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            fail("Service did not start")
        }
        context.root.getStore(ExtensionContext.Namespace.GLOBAL).put("docker-extension-$uuid", this)
    }

    override fun close() {
        stop()
    }
}
