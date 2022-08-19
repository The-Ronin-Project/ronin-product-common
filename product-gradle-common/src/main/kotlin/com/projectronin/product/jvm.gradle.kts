package com.projectronin.product

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    `maven-publish`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kover {
    engine.set(kotlinx.kover.api.DefaultJacocoEngine)

    xmlReport {
        reportFile.set(layout.buildDirectory.file("coverage/report.xml"))
    }

    htmlReport {
        reportDir.set(layout.buildDirectory.dir("coverage/html"))
    }
}

detekt {
    ignoreFailures = true
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    register<Test>("integrationTest") {
        group = "Verification"
        description = "Run integration tests matching format *IntegrationTest"
        filter {
            isFailOnNoMatchingTests = false
            includeTestsMatching("*IntegrationTest")
        }
    }

    register<Test>("unitTest") {
        group = "Verification"
        description = "Run unit tests matching format *UnitTest"
        filter {
            isFailOnNoMatchingTests = false
            includeTestsMatching("*UnitTest")
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
