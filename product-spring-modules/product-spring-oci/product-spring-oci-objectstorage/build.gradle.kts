plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.core)
    implementation(libs.spring.boot.autoconfigure)
    implementation(ronincommon.bucketstorage)
    implementation(ronincommon.oci.objectstorage)
    implementation(project(":product-spring-modules:product-spring-oci"))
}
