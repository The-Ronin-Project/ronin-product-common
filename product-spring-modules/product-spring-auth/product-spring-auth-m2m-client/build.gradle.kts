plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    api(project(":product-spring-modules:product-spring-jackson"))
    api(project(":product-spring-modules:product-spring-auth"))
    api(libs.okhttp)
    api(libs.retrofit.jackson)
    implementation(libs.kotlinlogging)
    implementation(libs.kotlin.coroutines.core)
    api(ronincommon.auth)
    api(ronincommon.auth.m2m.client)

    testImplementation(libs.assertj)
    testImplementation(libs.wiremock)
    testImplementation(libs.logback.core)
    testImplementation(libs.logback.classic)
    testImplementation(libs.kotlinx.coroutines.test)
}
