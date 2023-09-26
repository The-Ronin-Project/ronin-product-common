plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
    id(libs.plugins.kotlin.kapt.get().pluginId)
}

dependencies {
    api(project(":product-spring-modules:product-spring-auth"))
    implementation(platform(libs.spring.boot.bom))
    api(libs.spring.security.core)
    api(libs.spring.boot.core)
    api(libs.spring.security.resource.server)
    api(libs.spring.security.jose)
    api(project(":product-spring-modules:product-spring-exceptionhandling"))
    api(project(":product-spring-modules:product-spring-auth:product-spring-auth-seki-client"))
    implementation(libs.kotlinlogging)
    api(ronincommon.auth)

    testImplementation(libs.assertj)
    testImplementation(libs.wiremock)
    testImplementation(project(":product-spring-modules:product-spring-auth:product-spring-jwt-auth-testutils"))
    testImplementation(project(":product-spring-modules:product-spring-auth:product-spring-auth-seki-testutils"))

    kapt("org.springframework.boot:spring-boot-configuration-processor:" + libs.versions.springboot.get())
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:" + libs.versions.springboot.get())
}
