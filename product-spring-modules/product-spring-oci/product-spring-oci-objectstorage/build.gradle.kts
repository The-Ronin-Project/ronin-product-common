plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.core)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.boot.actuator)
    implementation(ronincommon.bucketstorage)
    implementation(ronincommon.oci.objectstorage)
    implementation("com.oracle.oci.sdk:oci-java-sdk-common-httpclient-jersey3")
    implementation(project(":product-spring-modules:product-spring-oci"))
    testImplementation(libs.bundles.spring.test)
}
