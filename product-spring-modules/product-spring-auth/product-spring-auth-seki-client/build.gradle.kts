plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
    id(libs.plugins.kotlin.kapt.get().pluginId)
}

dependencies {
    api(project(":product-spring-modules:product-spring-auth"))
    api(project(":product-spring-modules:product-spring-httpclient"))
    api(project(":product-spring-modules:product-spring-jackson"))
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.web)
    implementation(libs.spring.security.core)
    implementation(libs.jackson.kotlin)
    implementation(libs.kotlinlogging)
    api(ronincommon.auth)

    testImplementation(libs.mockk)
    testImplementation(libs.assertj)
    testImplementation(project(":product-spring-modules:product-spring-auth:product-spring-auth-seki-testutils"))

    kapt("org.springframework.boot:spring-boot-configuration-processor:" + libs.versions.springboot.get())
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:" + libs.versions.springboot.get())
}
