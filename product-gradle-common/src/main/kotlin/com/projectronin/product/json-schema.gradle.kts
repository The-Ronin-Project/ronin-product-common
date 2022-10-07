package com.projectronin.product

import org.jsonschema2pojo.GenerationConfig
import org.jsonschema2pojo.Jsonschema2Pojo
import org.jsonschema2pojo.gradle.GradleRuleLogger
import org.jsonschema2pojo.gradle.JsonSchemaExtension
import java.io.FileFilter
import java.net.URL

plugins {
    kotlin("jvm")
    java
}

dependencies {
    implementation("com.google.code.findbugs:jsr305:3.0.2")
}

abstract class JsonSchemaImportTask : DefaultTask() {
    private val schemaBaseUrl = "https://repo.devops.projectronin.io/repository/ronin-raw/"

    @get:Input
    abstract val schema: Property<String>

    @get:Input
    abstract val namespace: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val tag: Property<String>

    private val rootDir = project.file(project.buildDir.path + "/jsonschema/$name")
    private val sourcesDir = project.file(rootDir.path + "/sources")

    @get:OutputDirectory
    val generatedClasses: File = project.file(rootDir.path + "/generated")

    init {
        generatedClasses.mkdirs()
        project.tasks.compileJava {
            dependsOn(this@JsonSchemaImportTask)
        }
        project.tasks.compileKotlin {
            dependsOn(this@JsonSchemaImportTask)
        }

        project.sourceSets {
            main {
                java {
                    srcDir(generatedClasses)
                }
            }
        }
    }

    private val config: GenerationConfig by lazy {
        JsonSchemaExtension().apply {
            sourceFiles = listOf(sourcesDir)
            targetDirectory = generatedClasses
            targetPackage = "com.projectronin.product.jsonschema.generated.${namespace.get()}.${schema.get()}.${version.get()}".replace("-", "")

            includeConstructors = true
            includeAllPropertiesConstructor = true
            includeGetters = false
            includeSetters = false
            includeHashcodeAndEquals = true
            includeToString = true
            includeAdditionalProperties = true
            dateType = "java.time.LocalDate"
            dateTimeType = "java.time.LocalDateTime"
            initializeCollections = true // true=collection initialized as empty collection
            includeJsr305Annotations = true
            fileFilter = FileFilter { file ->
                // Ignore the files that aren't part of the schema itself
                file.path.matches("^(.*)\\.schema\\.json\$".toRegex())
            }
        }
    }

    @TaskAction
    fun fetchSchema() {
        rootDir.mkdirs()
        val artifactName = "${schema.get()}-${version.get()}-${tag.get()}.tar.gz"
        val artifactFile = project.file(rootDir.path + "/$artifactName")

        if (!artifactFile.exists()) {
            artifactFile.writeBytes(URL("$schemaBaseUrl${namespace.get()}/${schema.get()}/$artifactName").readBytes())
        }

        project.copy {
            from(project.tarTree(artifactFile))
            into(sourcesDir)
        }

        Jsonschema2Pojo.generate(config, GradleRuleLogger(project.logger))
    }
}

abstract class JsonSchemasExtension(private val project: Project) {
    fun schema(name: String, version: String, tag: String, namespace: String = "contract-event") {
        val formattedName = Regex("-[a-zA-Z]").replace("generate-${namespace}-${name}-${version}-Classes") {
            it.value.replace("-", "").toUpperCase()
        }
        project.tasks.create<JsonSchemaImportTask>(formattedName) {
            this.namespace.set(namespace)
            this.schema.set(name)
            this.version.set(version)
            this.tag.set(tag)
        }
    }
}

extensions.create<JsonSchemasExtension>("jsonSchema", project)
