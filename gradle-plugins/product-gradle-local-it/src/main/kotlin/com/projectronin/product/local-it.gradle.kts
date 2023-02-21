package com.projectronin.product

plugins {
    kotlin("jvm")
    java
    `jvm-test-suite`
}

testing {
    suites {
        val localContractTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(":product-gradle-local-it")
                implementation("org.testcontainers:testcontainers")
                implementation("com.github.tomakehurst:wiremock-jre8")
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
    expand("projectRoot" to project.rootDir)
}

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
    dependsOn(testing.suites.named("localContractTest"))
}
