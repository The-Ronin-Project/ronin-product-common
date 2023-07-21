@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(roningradle.plugins.buildconventions.gradledslplugin)
}

dependencies {
    api(libs.gradle.kotlin.jvm)
    api(libs.jsonschema2pojo)
}
