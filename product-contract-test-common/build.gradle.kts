plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    api(libs.bundles.testcontainers)
    api(libs.wiremock)
    api(libs.assertj)
    api(libs.okhttp)
    api(libs.slf4j.api)
    api(libs.logback.core)
    api(libs.logback.classic)
    api(libs.jackson.kotlin)
    api(project(":product-spring-modules:product-spring-auth:product-spring-auth-seki-testutils"))
    api(project(":product-spring-modules:product-spring-auth:product-spring-jwt-auth-testutils"))
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.test.boot)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.retry)
    testImplementation("com.mysql:mysql-connector-j")
}

tasks {
    processTestResources {
        expand(
            "projectDir" to project.projectDir,
            "projectRoot" to project.rootDir,
            "projectBuild" to project.buildDir
        )
    }
}
