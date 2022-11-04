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

    // TODO - REVIEW
    // Exclude any classes thet are within a 'generated' package
    // 1. below 'works', so confirmed this is correct location to modify
    // 2. *** Believe there's a "more appropriate syntax" to use (which I can't seem to figure out!)
    // 3. *** This may actually should be set on a "projbect by project" level
    //       instead of in common project.  tbd.
    // 4. it's also possible to filter based on classes that have a
    //      "@generated" annotation, but seemed more complicated. And would also
    //      have to confirm 'which @generated package' is the one it would look at.
    filters {
        classes {
            excludes += "*.generated.*"
        }
    }

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
