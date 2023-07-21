plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    api(platform(libs.spring.boot.bom))
    api(libs.spring.context)
    api(libs.spring.boot.autoconfigure)
    api(libs.okhttp)
    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.assertj)
}
