plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    api(libs.spring.security.core)
    api(libs.jackson.annotations)

    testImplementation(project(":product-spring-modules:product-spring-jackson"))
    testImplementation(libs.assertj)
}
