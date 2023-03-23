package com.projectronin.product.plugin.openapi

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.CodeGenerator
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.SourceApi
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.parser.core.models.ParseOptions
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
import java.net.URI
import java.net.URL
import java.nio.file.Path

interface OpenApiKotlinGeneratorInputSpec {
    @get:InputFile
    @get:Optional
    val inputFile: RegularFileProperty

    @get:Input
    @get:Optional
    val inputDependency: Property<String>

    @get:Input
    @get:Optional
    val inputUrl: Property<URL>

    @get:Input
    val packageName: Property<String>

    @get:Input
    @get:Optional
    val finalResourcePath: Property<String>
}

interface OpenApiKotlinGeneratorExtension {
    val generateClient: Property<Boolean>
    val generateModel: Property<Boolean>
    val generateController: Property<Boolean>
    val schemas: ListProperty<OpenApiKotlinGeneratorInputSpec>
    val outputDir: DirectoryProperty
    val resourcesOutputDirectory: DirectoryProperty
}

abstract class OpenApiKotlinGeneratorTask : DefaultTask() {

    @get:Input
    abstract val generateClient: Property<Boolean>

    @get:Input
    abstract val generateModel: Property<Boolean>

    @get:Input
    abstract val generateController: Property<Boolean>

    @get:Nested
    abstract val schemas: ListProperty<OpenApiKotlinGeneratorInputSpec>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val resourcesOutputDirectory: DirectoryProperty

    init {
        description = "Generates kotlin model, controller, and client classes from OpenAPI specifications"
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
            val inputUri: URI = if (input.inputFile.isPresent) {
                input.inputFile.get().asFile.toURI()
            } else if (input.inputUrl.isPresent) {
                input.inputUrl.get().toURI()
            } else if (input.inputDependency.isPresent) {
                val dependencySpec = "${input.inputDependency.get().replace("@.*$".toRegex(), "")}@json"
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
                dependencyFile.toURI()
            } else {
                throw IllegalArgumentException("Must specify either inputFile or inputUrl")
            }

            val outputPackageName = input.packageName
            val result = OpenAPIParser().readLocation(
                inputUri.toString(),
                null,
                ParseOptions().apply {
                    setResolve(true)
                }
            )

            val apiContent = Yaml.mapper().writeValueAsString(result.openAPI)

            val codeGenTypes: Set<CodeGenerationType> = setOfNotNull(
                if (generateClient.getOrElse(false)) CodeGenerationType.CLIENT else null,
                if (generateModel.getOrElse(true)) CodeGenerationType.HTTP_MODELS else null,
                if (generateController.getOrElse(true)) CodeGenerationType.CONTROLLERS else null,
            )
            val controllerOptions: Set<ControllerCodeGenOptionType> = emptySet()
            val modelOptions: Set<ModelCodeGenOptionType> = emptySet()
            val clientOptions: Set<ClientCodeGenOptionType> = emptySet()

            MutableSettings.updateSettings(codeGenTypes, controllerOptions, modelOptions, clientOptions)

            logger.info("Generating code for $inputUri to ${outputDir.get().asFile}")

            val packages = Packages(outputPackageName.get())
            val sourceApi = SourceApi.create(apiContent, emptyList())
            val generator = CodeGenerator(packages, sourceApi, Path.of(""), Path.of(""))
            generator.generate().forEach { it.writeFileTo(outputDir.get().asFile) }

            // because the generator hasn't been updated with the javax -> jakarta switch, brute-force that here
            outputDir.get().asFile.walk()
                .forEach {
                    if (it.name.endsWith(".kt")) {
                        it.writeText(
                            it.readText().replace("javax.", "jakarta."),
                        )
                    }
                }
        }
    }
}

class OpenApiKotlinGenerator : Plugin<Project> {
    override fun apply(project: Project) {

        val ex = project.extensions.create("generateOpenApiCode", OpenApiKotlinGeneratorExtension::class.java).apply {
            generateClient.convention(false)
            generateModel.convention(true)
            generateController.convention(true)
            outputDir.convention(project.layout.buildDirectory.dir("generated/openapi-kotlin-generator/kotlin"))
            resourcesOutputDirectory.convention(project.layout.buildDirectory.dir("generated/openapi-kotlin-generator/resources"))
        }

        (project.properties["sourceSets"] as SourceSetContainer?)?.getByName("main")?.java?.srcDir(ex.outputDir)
        (project.properties["sourceSets"] as SourceSetContainer?)?.getByName("main")?.resources?.srcDir(ex.resourcesOutputDirectory)

        project.tasks.register(
            "generateOpenApiCode",
            OpenApiKotlinGeneratorTask::class.java
        ) {
            group = BasePlugin.BUILD_GROUP
            generateClient.set(ex.generateClient)
            generateModel.set(ex.generateModel)
            generateController.set(ex.generateController)
            schemas.set(ex.schemas)
            outputDir.set(ex.outputDir)
            resourcesOutputDirectory.set(ex.resourcesOutputDirectory)
        }

        project.tasks.findByName("compileKotlin")?.apply {
            dependsOn("generateOpenApiCode")
        }

        project.tasks.findByName("processResources")?.apply {
            dependsOn("generateOpenApiCode")
        }

        project.tasks.findByName("runKtlintCheckOverMainSourceSet")?.apply {
            dependsOn("generateOpenApiCode")
        }
    }
}
