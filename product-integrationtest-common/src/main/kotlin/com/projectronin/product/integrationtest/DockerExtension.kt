package com.projectronin.product.integrationtest

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import java.time.Duration
import java.util.Properties
import java.util.UUID

class DockerExtension : BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    companion object {
        private val uuid: UUID = UUID.randomUUID()
        private val projectRootDir: File by lazy {
            val props = Properties()
            props.load(DockerExtension::class.java.classLoader.getResourceAsStream("test.properties"))
            File(props.getProperty("project.root"))
        }
        private val docker by lazy {
            DockerComposeContainer(File("$projectRootDir/docker-compose.yml"))
                .withExposedService(
                    "service", 1, 8080,
                    Wait.forHttp("/actuator").forPort(8080).forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(10))
                )
                .withExposedService(
                    "wiremock", 1, 8080,
                    Wait.forHttp("/__admin/mappings").forPort(8080).forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(10))
                )
                .withOptions("--profile wiremock --env-file $projectRootDir/wiremock.env")
        }
        private val started by lazy {
            docker.start()
            true
        }
        val serviceUrl by lazy {
            "${docker.getServiceHost("service_1", 8080)}:${docker.getServicePort("service_1", 8080)}"
        }
        val wiremockHost by lazy {
            docker.getServiceHost("wiremock_1", 8080)
        }
        val wiremockPort by lazy {
            docker.getServicePort("wiremock_1", 8080)
        }
        private val logger: Logger = LoggerFactory.getLogger(DockerExtension::class.java)
    }

    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            logger.error("Docker inexplicably not started")
        }
        context.root.getStore(ExtensionContext.Namespace.GLOBAL).put("docker-extension-$uuid", this)
    }

    override fun close() {
        docker.stop()
    }
}
