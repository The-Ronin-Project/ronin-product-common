plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    api(platform(libs.spring.boot.bom))
    api(libs.spring.boot.autoconfigure)
    api(libs.spring.boot.core)
    api(libs.spring.web)
    api(libs.jackson.kotlin)
    api(libs.jakarta.validation.api)
}
