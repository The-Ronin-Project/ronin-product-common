plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.jackson.annotations)
    testImplementation(project(":product-spring-modules:product-spring-jackson"))
    testImplementation(libs.assertj)
    testImplementation(libs.jackson.kotlin)
}
