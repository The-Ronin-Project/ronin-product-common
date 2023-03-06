package com.projectronin.product.contracttest.services

import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import com.projectronin.product.contracttest.LocalContractTestExtension
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.fail
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.test.util.TestSocketUtils
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
    val jarDirectorySubPath: String = "libs"
) : ContractTestService {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private var process: Process? = null

    val servicePort: Int = TestSocketUtils.findAvailableTcpPort()

    val serviceUrl: String = "http://localhost:$servicePort"

    override val started: Boolean
        get() = process != null

    override val replacementTokens: Map<String, String>
        get() = emptyMap()

    override fun start() {
        synchronized(this) {
            if (process == null) {
                logger.info("Starting service under test")

                val tempApplicationPropertiesFile: File = run {
                    val f = File.createTempFile("contract-test", ".properties")
                    f.deleteOnExit()
                    f
                }

                val props = Properties()
                props.load(LocalContractTestExtension::class.java.classLoader.getResourceAsStream("test.properties"))

                val projectBuildDir: File = File(props.getProperty("project.build.dir"))
                val projectDir: File = File(props.getProperty("project.dir"))

                val logOutputDir = File(projectBuildDir, "contract-test-service-logs")
                if (!logOutputDir.exists()) {
                    logOutputDir.mkdirs()
                }

                when (val resource = LocalContractTestExtension::class.java.classLoader.getResource("application-test.properties")) {
                    null -> fail("Must have a application-test.properties on the localContractTest classpath")
                    else -> {
                        val importedText = dependentServices.flatMap { it.replacementTokens.entries }
                            .fold(resource.readText()) { text, entry ->
                                text.replace("{{${entry.key}}}", entry.value)
                            }
                        tempApplicationPropertiesFile.writeText(
                            """
                            |$importedText
                            |server.port=$servicePort
                            """.trimMargin("|")
                        )
                    }
                }

                logger.info("Log output will be available at $logOutputDir")

                process = run {
                    val javaCommand = "${System.getProperty("java.home")}/bin/java"
                    when (val jarFile = (File(projectBuildDir, jarDirectorySubPath).listFiles() ?: arrayOf<File>()).firstOrNull { f -> f.extension == "jar" && !f.name.contains("plain") }) {
                        null -> fail("Must be able to find spring boot jar")
                        else ->
                            ProcessBuilder(
                                javaCommand,
                                "-jar",
                                jarFile.absolutePath,
                                "--spring.config.location=${tempApplicationPropertiesFile.absolutePath}"
                            )
                                .directory(projectDir)
                                .redirectOutput(File(logOutputDir, "stdout.log"))
                                .redirectError(File(logOutputDir, "stderr.log"))
                                .start()
                    }
                }

                runBlocking {
                    retry(limitAttempts(30) + constantDelay(1000)) {
                        verifyService()
                    }
                }
            }
        }
    }

    private fun verifyService() {
        LocalContractTestExtension.httpClient.newCall(
            Request.Builder()
                .url("$serviceUrl/actuator")
                .get()
                .build()
        ).execute().use { response ->
            Assertions.assertThat(response.code).isEqualTo(200).withFailMessage { "Service not available on port $servicePort" }
        }
    }

    override fun stopSafely() {
        synchronized(this) {
            if (process?.isAlive == true) {
                process?.runCatching { destroyForcibly() }
                    ?.onFailure { e -> logger.error("Spring Service did not stop", e) }
            }
        }
    }
}
