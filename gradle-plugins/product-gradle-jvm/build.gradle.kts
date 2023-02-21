@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.kover)
}

group = rootProject.group
version = libs.versions.product.common.get()

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

kover {
    engine.set(kotlinx.kover.api.DefaultIntellijEngine)
}

dependencies {
    api(libs.gradle.kotlin.jvm)
    api(libs.gradle.kover)
    api(libs.gradle.detekt)
    api(libs.gradle.ktlint)
}

repositories {
    gradlePluginPortal()
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
