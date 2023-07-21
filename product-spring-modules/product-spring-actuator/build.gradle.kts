plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(project(":product-spring-modules:product-spring-module-base"))
    implementation(platform(libs.spring.boot.bom))
    api(libs.spring.boot.actuator)
}
