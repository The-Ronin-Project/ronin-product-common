package com.projectronin.product.contracttest.services

import com.projectronin.contracttest.standardSpringBootJarFile
import com.projectronin.contracttest.withServiceUnderTest
import com.projectronin.domaintest.DomainTestSetupContext
import com.projectronin.domaintest.SupportingServices
import com.projectronin.domaintest.exposedServicePort
import com.projectronin.domaintest.externalUriFor
import com.projectronin.domaintest.externalWiremockPort
import com.projectronin.domaintest.internalWiremockUrl
import org.junit.jupiter.api.fail
import org.testcontainers.containers.GenericContainer
import java.io.File
import java.util.Properties

/**
 * Runs a spring boot app from the `build/libs` directory.  Meant to start up the service you're trying to contract-test, and thus must be provided with a list
 * of the dependent services.  Hunts for the spring boot jar, so you have to run `./gradlew bootJar` first.
 *
 * Depends on there being a `application-test.properties` and a `test.properties` on the classpath.
 *
 * **test.properties**: needs to contain project.build.dir pointing at the project's build dir, and project.dir, which is used as the working dir for the app.
 *
 * **application-test.properties**: Needs to contain properties for the spring boot app that need to change for the test run.  Specifically, DB connection strings,
 * external service URIs, etc.
 */
class ContractTestServiceUnderTest(
    override val dependentServices: List<ContractTestService>,
    private val jarDirectorySubPath: String = "libs",
    private val testConfigResourceName: String = "application-test.properties",
    private val additionalArguments: List<String> = emptyList(),
    private val extraContainerConfiguration: (GenericContainer<*>.() -> GenericContainer<*>)? = null
) : ContractTestService {

    // spring.profiles.active=test
    // testConfigResourceName = "application-test.yaml"

    val servicePort: Int
        get() = exposedServicePort("service", 8080)

    val serviceUrl: String
        get() = externalUriFor("service")

    override val started: Boolean
        get() = true

    override val replacementTokens: Map<String, String>
        get() = emptyMap()

    override val internalReplacementTokens: Map<String, String>
        get() = replacementTokens

    private val jarFile: File
        get() = runCatching { standardSpringBootJarFile }
            .getOrElse {
                (File(projectBuildDir, jarDirectorySubPath).listFiles() ?: arrayOf<File>())
                    .firstOrNull { f -> f.extension == "jar" && !f.name.contains("javadoc|sources|plain".toRegex()) } ?: throw IllegalStateException("Couldn't find jar file under $projectBuildDir")
            }

    private fun generateConfigFileText(): String {
        return when (val resource = javaClass.classLoader.getResource(testConfigResourceName)) {
            null -> fail("Must have a $testConfigResourceName on the localContractTest classpath")
            else -> {
                val rawText = resource.readText()
                (dependentServices.flatMap { it.internalReplacementTokens.entries.map { entry -> entry.key to entry.value } } + setOf("servicePort" to "8080"))
                    .flatMap { incomingPair ->
                        when (incomingPair.first) {
                            "wireMockPort" -> listOf(
                                "http://127.0.0.1:{{wireMockPort}}" to internalWiremockUrl(""),
                                "http://localhost:{{wireMockPort}}" to internalWiremockUrl(""),
                                "{{wireMockPort}}" to "8080",
                                "{{wireMockExternalPort}}" to "$externalWiremockPort"
                            )

                            "mySqlPort" -> listOf(
                                "localhost:{{mySqlPort}}" to "${SupportingServices.MySql.containerName}:3306"
                            )

                            "mySqlJdbcUri" -> listOf(
                                "{{mySqlJdbcUri}}" to incomingPair.second.replace("localhost:\\d+", "${SupportingServices.MySql.containerName}:3306")
                            )

                            "mySqlR2dbcUri" -> listOf(
                                "{{mySqlR2dbcUri}}" to incomingPair.second.replace("localhost:\\d+", "${SupportingServices.MySql.containerName}:3306")
                            )

                            else -> listOf(Pair("{{${incomingPair.first}}}", incomingPair.second))
                        }
                    }
                    .fold(rawText) { text, entry ->
                        text.replace(entry.first, entry.second)
                    }
            }
        }
    }

    private val projectBuildDir: File by lazy {
        val props = Properties()
        props.load(javaClass.classLoader.getResourceAsStream("test.properties"))
        File(props.getProperty("project.build.dir"))
    }

    override fun setupAgainstDomainTest(): DomainTestSetupContext.() -> Unit = {
        withServiceUnderTest(jarFile) {
            withActiveSpringProfiles("domaintest")
            dependentServices.forEach {
                when (it) {
                    is ContractTestKafkaService -> withKafka()
                    is ContractTestMySqlService -> withMySQL()
                    is ContractTestWireMockService -> withWireMock()
                }
            }
            if (testConfigResourceName.endsWith("properties")) {
                @Suppress("DEPRECATION")
                configPropertiesProvider(::generateConfigFileText)
            } else {
                configYamlProvider(::generateConfigFileText)
            }
            extraContainerConfiguration?.let { extraConfiguration(it) }
        }
    }
}
