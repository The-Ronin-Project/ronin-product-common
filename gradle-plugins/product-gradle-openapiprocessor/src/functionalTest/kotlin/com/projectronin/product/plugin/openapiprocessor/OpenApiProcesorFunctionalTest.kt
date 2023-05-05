package com.projectronin.product.plugin.openapiprocessor

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipFile

class OpenApiProcesorFunctionalTest {

    @TempDir
    lateinit var tempDir: Path

    private fun getProjectDir() = tempDir.toFile()
    private fun getBuildFile() = getProjectDir().resolve("build.gradle.kts")
    private fun getSettingsFile() = getProjectDir().resolve("settings.gradle.kts")

    @Test
    fun `can run task`() {
        // Setup the test build
        getSettingsFile().writeText("")
        getBuildFile().writeText(
            """
plugins {
    id("com.projectronin.product.openapiprocessor")
}
"""
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("processOpenApi")
        runner.withProjectDir(getProjectDir())
        val result = runner.build();

        // Verify the result
        with(result.output) {
            assertThat(this).contains("Task :processOpenApi")
            assertThat(this).contains("BUILD SUCCESSFUL")
            assertThat(this).contains("1 actionable task: 1 executed")
        }
    }

    @Test
    fun `can generate code using a dependency`() {
        val resource = javaClass.classLoader.getResource("dependency-generation-test/settings.gradle.kts")
        assertThat(resource).describedAs("Resource must not be null").isNotNull()
        val generationTestInputDirectory = File(resource!!.file).parentFile
        generationTestInputDirectory.copyRecursively(getProjectDir())

        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("assemble")
        runner.withProjectDir(getProjectDir())
        val result = runner.build()
        tempDir.resolve("app/build/generated/openapiprocessor").toFile().walk().forEach {
            f -> println(f)
        }

        assertThat(tempDir.toFile().resolve("app/build/generated/openapiprocessor/resources/META-INF/resources/v1/questionnaire.yml")).exists()
        assertThat(tempDir.toFile().resolve("app/build/generated/openapiprocessor/java/com/projectronin/services/questionnaire/api/v1/model/QuestionGroup.java")).exists()

        val entry = ZipFile(tempDir.toFile().resolve("app/build/libs/app.jar")).getEntry("META-INF/resources/v1/questionnaire.yml")
        assertThat(entry).isNotNull()
        assertThat(entry.compressedSize).isGreaterThan(1000L)

        // Verify the result
        with(result.output) {
            assertThat(this).contains("BUILD SUCCESSFUL")
        }
    }
}
