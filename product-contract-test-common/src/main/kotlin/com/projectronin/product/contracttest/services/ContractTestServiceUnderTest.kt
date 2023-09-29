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
import java.lang.management.ManagementFactory
import java.util.Properties
import java.util.UUID

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
    private val debug: Boolean = false,
    private val debugPort: Int = 5005,
    private val debugSuspend: Boolean = true,
    private val additionalArguments: List<String> = emptyList(),
    private val environmentVariables: Map<String, String> = emptyMap()
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

                val testSetupData = configureTestDirectoriesAndFiles()

                configureSpringConfigFile(testSetupData)

                process = startProcess(testSetupData, constructProcessArguments(testSetupData, debug, debugPort, debugSuspend, additionalArguments), environmentVariables)

                runBlocking {
                    runCatching {
                        retry(limitAttempts(300) + constantDelay(1000)) {
                            verifyService()
                        }
                    }
                        .onFailure { t ->
                            throw RuntimeException("Failed to verify service: ${File(testSetupData.logOutputDir, "stdout.log").readText()}", t)
                        }
                }
            }
        }
    }

    private fun constructProcessArguments(
        setupData: TestSetupData,
        debug: Boolean,
        debugPort: Int,
        debugSuspend: Boolean,
        additionalArguments: List<String>
    ): List<String> {
        val processArguments = mutableListOf<String>()
        processArguments += additionalArguments
        if (debug) {
            processArguments += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=${if (debugSuspend) "y" else "n"},address=*:$debugPort"
        }

        val runtimeMxBean = ManagementFactory.getRuntimeMXBean()
        val arguments = runtimeMxBean.inputArguments

        val ideaArguments = arguments.filter { it.matches("""-D.*coverage.*""".toRegex()) }
        val javaAgentArgument = arguments.firstOrNull { it.matches("""-javaagent.*?(intellij-coverage-agent.*?\.jar|jacocoagent.jar).*""".toRegex()) }

        if (javaAgentArgument != null) {
            val sourceJarLocation = File(javaAgentArgument.replace(".*-javaagent:(.*?(intellij-coverage-agent.*?\\.jar|jacocoagent.jar)).*".toRegex(), "$1"))
            val destinationJarLocation = run {
                val tf = File.createTempFile("coverage-agent", ".jar")
                tf.deleteOnExit()
                sourceJarLocation.copyTo(tf, overwrite = true)
                tf
            }

            val newJavaAgentArgument = javaAgentArgument
                .replace("-javaagent:.*?(intellij-coverage-agent.*?\\.jar|jacocoagent.jar)".toRegex(), "-javaagent:$destinationJarLocation")
                .replace("build/jacoco/.*?\\.exec".toRegex(), "${setupData.projectBuildDir.absolutePath}/jacoco/test-${UUID.randomUUID()}.exec")

            processArguments += newJavaAgentArgument
            processArguments += ideaArguments
        }

        return processArguments
    }

    private fun startProcess(testSetupData: TestSetupData, additionalArguments: List<String>, environmentVariables: Map<String, String>): Process? {
        val javaCommand = "${System.getProperty("java.home")}/bin/java"
        return when (
            val jarFile = (File(testSetupData.projectBuildDir, jarDirectorySubPath).listFiles() ?: arrayOf<File>())
                .firstOrNull { f -> f.extension == "jar" && !f.name.contains("javadoc|sources|plain".toRegex()) }
        ) {
            null -> fail("Must be able to find spring boot jar")
            else -> {
                val commands = mutableListOf<String>()
                commands += javaCommand
                commands += additionalArguments
                commands += listOf(
                    "-jar",
                    jarFile.absolutePath,
                    "--spring.config.location=${testSetupData.tempApplicationPropertiesFile.absolutePath}"
                )
                val processBuilder = ProcessBuilder(*commands.toTypedArray())
                    .directory(testSetupData.projectDir)
                    .redirectOutput(File(testSetupData.logOutputDir, "stdout.log"))
                    .redirectError(File(testSetupData.logOutputDir, "stderr.log"))

                processBuilder.environment() += environmentVariables

                processBuilder
                    .start()
            }
        }
    }

    private fun configureSpringConfigFile(testSetupData: TestSetupData) {
        when (val resource = LocalContractTestExtension::class.java.classLoader.getResource(testConfigResourceName)) {
            null -> fail("Must have a $testConfigResourceName on the localContractTest classpath")
            else -> {
                val importedText = (dependentServices.flatMap { it.replacementTokens.entries.map { entry -> entry.key to entry.value } } + setOf("servicePort" to servicePort.toString()))
                    .fold(resource.readText()) { text, entry ->
                        text.replace("{{${entry.first}}}", entry.second)
                    }
                testSetupData.tempApplicationPropertiesFile.writeText(
                    """
                    |$importedText
                    |${if (testConfigResourceName.endsWith(".properties")) "server.port=$servicePort" else "server:\n  port: $servicePort"}
                    """.trimMargin("|")
                )
            }
        }
    }

    private fun configureTestDirectoriesAndFiles(): TestSetupData {
        val props = Properties()
        props.load(LocalContractTestExtension::class.java.classLoader.getResourceAsStream("test.properties"))

        return TestSetupData(
            tempApplicationPropertiesFile = run {
                val f = File.createTempFile("contract-test", testConfigResourceName.replace(""".*\.""".toRegex(), "."))
                f.deleteOnExit()
                f
            },
            projectBuildDir = File(props.getProperty("project.build.dir")),
            projectDir = File(props.getProperty("project.dir")),
            logOutputDir = File(File(props.getProperty("project.build.dir")), "contract-test-service-logs").apply {
                if (!exists()) {
                    mkdirs()
                }
                logger.info("Log output will be available at $this")
            }
        )
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
                process?.runCatching { destroy() }
                    ?.onFailure { e -> logger.error("Spring Service did not stop", e) }
            }
        }
    }

    private class TestSetupData(
        val tempApplicationPropertiesFile: File,
        val projectBuildDir: File,
        val projectDir: File,
        val logOutputDir: File
    )
}
