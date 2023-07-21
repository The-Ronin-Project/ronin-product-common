plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    api(platform(libs.spring.boot.bom))
    api(libs.bundles.jackson)
    testImplementation(libs.assertj)
    testImplementation(libs.mockk)
}
