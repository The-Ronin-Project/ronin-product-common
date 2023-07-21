plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(libs.kotlinlogging)
    implementation(platform(libs.spring.boot.bom))
    api(libs.spring.context)
    api(libs.spring.boot.autoconfigure)
    testImplementation(libs.bundles.spring.test)
}
