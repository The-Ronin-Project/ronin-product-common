package com.projectronin.product.contracttest

import com.projectronin.product.contracttest.services.ContractServicesProvider
import com.projectronin.product.contracttest.services.ContractTestMySqlService
import com.projectronin.product.contracttest.services.ContractTestService
import com.projectronin.product.contracttest.services.ContractTestServiceUnderTest
import com.projectronin.product.contracttest.services.ContractTestWireMockService
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermissions
import java.time.Duration
import java.util.Properties

/**
 * An example of a ContractServicesProvider that starts up a spring service with a database and a wiremock to handle back-end services.  One of these implementations needs to be
 * present in your localContractTest source packages.
 */
class StudentDataServicesProvider : ContractServicesProvider {
    override fun provideServices(): List<ContractTestService> {
        // ordinarily, we'd have an already-built spring boot jar in our project.  But this one doesn't, so, we're going to fake it by exfiltrating one
        // from our blueprint image.  In a normal spring boot project, this would be unnecessary

        val props = Properties()
        props.load(LocalContractTestExtension::class.java.classLoader.getResourceAsStream("test.properties"))

        val libSubDirectory = "blueprint-libs"
        val projectBuildDir: File = File(props.getProperty("project.build.dir"))
        val libDir = File(projectBuildDir, libSubDirectory)

        if (libDir.exists()) {
            libDir.deleteRecursively()
        }

        val ownerWritable = PosixFilePermissions.fromString("rwxrwxrwx")
        val permissions: FileAttribute<*> = PosixFilePermissions.asFileAttribute(ownerWritable)
        Files.createDirectory(libDir.toPath(), permissions)

        val container = GenericContainer(DockerImageName.parse("docker-repo.devops.projectronin.io/student-data-service:7ced907ae71fb263d435a38e3d3302681fae9eb1"))
            .withCreateContainerCmdModifier { cmd -> cmd.withEntrypoint("/bin/bash") }
            .withCommand("-c", "cp /app/app.jar /library-output && echo 'completed'")
            .withFileSystemBind(libDir.absolutePath, "/library-output")
            .waitingFor(Wait.forLogMessage(".*completed.*", 1))
            .withStartupTimeout(Duration.ofSeconds(300))
        kotlin.runCatching { container.start() }
            .onFailure { e ->
                println(container.getLogs())
                throw e
            }
        container.stop()

        // end part that isn't necessary in a normal project

        // this is a typical service setup, with the exceptions noted in the comments below.
        return listOf(
            ContractTestServiceUnderTest(
                dependentServices = listOf(
                    ContractTestWireMockService(),
                    ContractTestMySqlService(
                        "test",
                        "test",
                        "student_data"
                    )
                ),
                // ordinarily you wouldn't need to override the default here either.
                jarDirectorySubPath = libSubDirectory,
                testConfigResourceName = "application-test.yml"
            )
        )
    }
}
