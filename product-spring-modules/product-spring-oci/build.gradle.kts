plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.core)
    api(ronincommon.oci)
    testImplementation(libs.bundles.spring.test)
}
