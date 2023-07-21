plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    api(project(":product-spring-modules:product-spring-actuator"))
    api(project(":product-spring-modules:product-spring-auth:product-spring-jwt-auth:product-spring-jwt-auth-webflux"))
    api(project(":product-spring-modules:product-spring-cors:product-spring-cors-webflux"))
    api(project(":product-spring-modules:product-spring-exceptionhandling"))
    api(project(":product-spring-modules:product-spring-exceptionhandling:product-spring-exceptionhandling-webflux"))
    api(project(":product-spring-modules:product-spring-fhir"))
    api(project(":product-spring-modules:product-spring-jackson:product-spring-jackson-web"))
    api(project(":product-spring-modules:product-spring-httpclient"))
    api(project(":product-spring-modules:product-spring-openapi-generated"))
    api(project(":product-spring-modules:product-spring-telemetry"))
    api(project(":product-spring-modules:product-spring-logging"))

    api(libs.bundles.spring.webflux)
    api(libs.okhttp)
}
