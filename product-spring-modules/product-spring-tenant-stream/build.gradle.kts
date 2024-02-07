plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.jakarta.annotation.api)
    testImplementation(libs.micrometer.statsd)
    api(libs.spring.boot.autoconfigure)
    api(ronincommon.tenant)

    testImplementation(libs.bundles.spring.test)
}
