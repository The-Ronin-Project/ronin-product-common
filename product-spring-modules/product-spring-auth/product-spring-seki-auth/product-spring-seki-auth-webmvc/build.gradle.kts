plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.bundles.spring.web)
    api(project(":product-spring-modules:product-spring-auth"))
    api(project(":product-spring-modules:product-spring-auth:product-spring-seki-auth"))
    api(ronincommon.auth)

    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.assertj)
}
