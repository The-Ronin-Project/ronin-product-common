plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.core)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.boot.webflux)
    api(project(":product-spring-modules:product-spring-cors"))

    testImplementation(libs.bundles.spring.webflux)
    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.assertj)
}
