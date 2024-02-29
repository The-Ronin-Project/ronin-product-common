plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    api(project(":product-spring-modules:product-spring-oci"))
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.core)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.boot.actuator)
    api(ronincommon.oci.objectstorage)
    api(ronincommon.ocisdk.objectstorage)
    testImplementation(libs.bundles.spring.test)
}
