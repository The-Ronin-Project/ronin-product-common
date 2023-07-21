plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(project(":product-spring-modules:product-spring-module-base"))
    api(libs.spring.context)
    api(libs.spring.boot.autoconfigure)
    implementation(libs.kotlinlogging)
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.datadog.api)
    testImplementation(libs.bundles.spring.test)
}
