plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
    id(libs.plugins.kotlin.kapt.get().pluginId)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))

    api(libs.spring.context)
    api(libs.spring.boot.autoconfigure)
    implementation(libs.kotlinlogging)
    implementation(project(":product-spring-modules:product-spring-module-base"))
    api(project(":product-spring-modules:product-spring-auth"))
    api(project(":product-spring-modules:product-spring-kafka"))
    implementation("com.projectronin.product.audit.messaging:contract-messaging-audit:1.0.0")

    kapt("org.springframework.boot:spring-boot-configuration-processor:" + libs.versions.springboot.get())

    testImplementation(libs.bundles.spring.test)
}

ktlint {
    filter {
        exclude("**/jsonschema/**")
    }
}
