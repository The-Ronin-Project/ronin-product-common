plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    api(project(":product-spring-modules:product-spring-auth"))
    api(project(":product-spring-modules:product-spring-auth:product-spring-auth-seki-client"))
    api(project(":product-spring-modules:product-spring-httpclient"))
    api(project(":product-spring-modules:product-spring-jackson"))
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.core)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.jackson.kotlin)
    implementation(libs.kotlinlogging)
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.web)

    testImplementation(libs.mockk)
    testImplementation(libs.assertj)
    testImplementation(project(":product-spring-modules:product-spring-jackson"))
}
