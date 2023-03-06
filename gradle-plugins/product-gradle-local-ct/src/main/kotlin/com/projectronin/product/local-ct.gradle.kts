package com.projectronin.product

plugins {
    kotlin("jvm")
    java
    `jvm-test-suite`
}

testing {
    suites {
        @Suppress("UNUSED_VARIABLE")
        val localContractTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(project())
                implementation("org.testcontainers:testcontainers")
                implementation("org.testcontainers:mysql")
                implementation("org.testcontainers:kafka")
                implementation("com.github.tomakehurst:wiremock-jre8-standalone")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
            }

            testType.set(TestSuiteType.INTEGRATION_TEST)

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter("test") }
                }
            }
        }
    }
}

tasks.getByName("localContractTest") {
    dependsOn("bootJar")
}

tasks.getByName("processLocalContractTestResources", org.gradle.language.jvm.tasks.ProcessResources::class) {
    expand(
        "projectDir" to project.projectDir,
        "projectRoot" to project.rootDir,
        "projectBuild" to project.buildDir,
    )
}

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
    dependsOn(testing.suites.named("localContractTest"))
}
