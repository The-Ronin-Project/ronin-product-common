plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.bundles.spring.web)
    implementation(libs.opentracing.util)
    implementation(ronincommon.common)
    api(project(":product-spring-modules:product-spring-auth:product-spring-jwt-auth"))
    api(project(":product-spring-modules:product-spring-auth:product-spring-auth-seki-client"))
    api(ronincommon.auth)

    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.assertj)
    testImplementation(libs.wiremock)
    testImplementation(project(":product-spring-modules:product-spring-auth:product-spring-auth-seki-testutils"))
    testImplementation(project(":product-spring-modules:product-spring-exceptionhandling:product-spring-exceptionhandling-webmvc"))
    testImplementation(project(":product-spring-modules:product-spring-auth:product-spring-jwt-auth:product-spring-jwt-auth-mocks"))
}
