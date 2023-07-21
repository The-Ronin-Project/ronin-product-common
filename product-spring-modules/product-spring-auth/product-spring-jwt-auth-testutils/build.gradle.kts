plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    api(libs.bundles.spring.test)
    api(libs.assertj)
    api(libs.wiremock)
    api(project(":product-spring-modules:product-spring-auth"))
    api(libs.spring.security.jose)
    api(project(":product-spring-modules:product-spring-jackson"))
}
