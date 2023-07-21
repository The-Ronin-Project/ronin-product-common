@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(roningradle.plugins.buildconventions.gradledslplugin)
}

dependencies {
    api(libs.gradle.kotlin.jvm)
    api(libs.gradle.kotlin.allopen)
    api(libs.gradle.kotlin.noarg)
    api(libs.gradle.springboot)
    api(libs.gradle.springdepmanager)
}
