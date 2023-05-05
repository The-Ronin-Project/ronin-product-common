package com.projectronin.product.plugin.openapiprocessor

import io.openapiprocessor.core.parser.ParserType
import io.openapiprocessor.core.writer.DefaultWriterFactory
import io.openapiprocessor.spring.processor.SpringProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import java.io.File

interface OpenApiProcessorInputSpec {
    @get:Input
    val inputDependency: Property<String>

    @get:Input
    @get:Optional
    val packageName: Property<String>

    @get:Input
    @get:Optional
    val finalResourcePath: Property<String>

    @get:Input
    @get:Optional
    val parser: Property<ParserType>

    @get:InputFile
    @get:Optional
    val mapping: RegularFileProperty
}

interface OpenApiProcessorExtension {
    val schemas: ListProperty<OpenApiProcessorInputSpec>
    val outputDir: DirectoryProperty
    val resourcesOutputDirectory: DirectoryProperty
}

abstract class OpenApiProcessorTask : DefaultTask() {

    @get:Nested
    abstract val schemas: ListProperty<OpenApiProcessorInputSpec>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val resourcesOutputDirectory: DirectoryProperty

    init {
        description = "Generates kotlin model classes and controller interfaces from OpenAPI specifications"
        group = BasePlugin.BUILD_GROUP
    }

    @TaskAction
    fun generateOpenApi() {

        with(outputDir.get().asFile) {
            if (!exists()) {
                mkdirs()
            }
        }

        schemas.get().forEach { input ->
            val inputFile: File = run {
                val dependencySpec = "${input.inputDependency.get().replace("@.*$".toRegex(), "")}@yaml"
                val configName = dependencySpec.split(":")[1].replace("[^A-Za-z]".toRegex(), "_")
                project.configurations.maybeCreate(configName)
                project.dependencies.add(configName, dependencySpec)
                val dependencyFile = project.configurations.getByName(configName).resolve().first()
                if (input.finalResourcePath.isPresent && resourcesOutputDirectory.isPresent) {
                    val finalResourcePath = if (input.finalResourcePath.get().startsWith("META-INF/resources")) {
                        input.finalResourcePath.get()
                    } else {
                        "META-INF/resources/${input.finalResourcePath.get().replace("^/".toRegex(), "")}"
                    }
                    if (resourcesOutputDirectory.get().asFile.exists()) {
                        resourcesOutputDirectory.get().asFile.delete()
                    }
                    resourcesOutputDirectory.get().asFile.mkdirs()
                    dependencyFile.copyTo(resourcesOutputDirectory.get().file(finalResourcePath).asFile, true)
                }
                dependencyFile
            }

            val processor = SpringProcessor(DefaultWriterFactory())
            val optionsMap = mutableMapOf<String, Any?>()
            optionsMap["apiPath"] = inputFile.absolutePath
            optionsMap["targetDir"] = outputDir.get().asFile.absolutePath
            if (input.parser.isPresent) {
                optionsMap["parser"] = input.parser.get().name
            }

            optionsMap["mapping"] = if (input.mapping.isPresent) {
                input.mapping.get().asFile.absolutePath
            } else {
                val tf = File.createTempFile("openapiprocessing", ".yaml")
                tf.deleteOnExit()
                val packageName = if (input.packageName.isPresent) {
                    input.packageName.get()
                } else {
                    val segments = input.inputDependency.get().replace("@.*$".toRegex(), "").split(":")
                    val dependencyPackage = "${segments[1].lowercase().replace("[^a-z0-9]+".toRegex(), ".")}.api.v${segments[2].replace("\\..*", "").replace("[^0-9]".toRegex(), "")}"
                    "com.projectronin.$dependencyPackage"
                }
                // language=YAML
                tf.writeText(
                    """
                        openapi-processor-mapping: v3
                        options:
                          package-name: $packageName
                          one-of-interface: true
                          bean-validation: jakarta
                          generated-date: true
                          format-code: false
                          javadoc: true
                    """.trimIndent()
                )
                tf.absolutePath
            }
            processor.run(optionsMap)

        }
    }
}

class OpenApiProcessor : Plugin<Project> {
    override fun apply(project: Project) {

        val ex = project.extensions.create("processOpenApi", OpenApiProcessorExtension::class.java).apply {
            outputDir.convention(project.layout.buildDirectory.dir("generated/openapiprocessor/java"))
            resourcesOutputDirectory.convention(project.layout.buildDirectory.dir("generated/openapiprocessor/resources"))
        }

        (project.properties["sourceSets"] as SourceSetContainer?)?.getByName("main")?.java?.srcDir(ex.outputDir)
        (project.properties["sourceSets"] as SourceSetContainer?)?.getByName("main")?.resources?.srcDir(ex.resourcesOutputDirectory)

        project.tasks.register(
            "processOpenApi",
            OpenApiProcessorTask::class.java
        ) {
            group = BasePlugin.BUILD_GROUP
            schemas.set(ex.schemas)
            outputDir.set(ex.outputDir)
            resourcesOutputDirectory.set(ex.resourcesOutputDirectory)
        }

        project.tasks.findByName("compileKotlin")?.apply {
            dependsOn("processOpenApi")
        }

        project.tasks.findByName("processResources")?.apply {
            dependsOn("processOpenApi")
        }

        project.tasks.findByName("runKtlintCheckOverMainSourceSet")?.apply {
            dependsOn("processOpenApi")
        }
    }
}
