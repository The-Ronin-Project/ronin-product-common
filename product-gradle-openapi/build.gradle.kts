@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `kotlin-dsl`
    `maven-publish`
    `java-gradle-plugin`
    alias(libs.plugins.kover)
}

group = rootProject.group
version = libs.versions.product.common.get()

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

kover {
    engine.set(kotlinx.kover.api.DefaultJacocoEngine)
}

dependencies {
    api(libs.bundles.gradleplugins.minimal)
    implementation(libs.swaggerparser)
    implementation(libs.fabrikt)

    testImplementation(libs.assertj)
}

repositories {
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("openApiKotlinGenerator") {
            id = "com.projectronin.product.plugin.openapi"
            implementationClass = "com.projectronin.product.plugin.openapi.OpenApiKotlinGenerator"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "nexus"
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_TOKEN")
            }
            url = if (project.version.toString().endsWith("SNAPSHOT")) {
                uri("https://repo.devops.projectronin.io/repository/maven-snapshots/")
            } else {
                uri("https://repo.devops.projectronin.io/repository/maven-releases/")
            }
        }
    }
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useJUnitJupiter()
        }

        // Create a new test suite
        val functionalTest by registering(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useJUnitJupiter()

            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(project)
                implementation(libs.assertj)
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) }
                }
            }
        }
    }
}

gradlePlugin.testSourceSets(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
    dependsOn(testing.suites.named("functionalTest"))
}
