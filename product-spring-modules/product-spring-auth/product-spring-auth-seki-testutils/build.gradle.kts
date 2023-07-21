plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(libs.wiremock)
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.jackson.kotlin)
}
