plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.bundles.spring.webflux)
    api(project(":product-spring-modules:product-spring-exceptionhandling"))
    api(project(":product-spring-modules:product-spring-auth"))

    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.assertj)
    testImplementation(project(":product-spring-modules:product-spring-auth:product-spring-seki-auth:product-spring-seki-auth-webflux"))
    testImplementation(project(":product-spring-modules:product-spring-jackson:product-spring-jackson-web"))
    testImplementation(project(":product-spring-modules:product-spring-auth:product-spring-seki-auth"))
}
