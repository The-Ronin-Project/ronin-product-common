plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
    id(libs.plugins.kotlin.kapt.get().pluginId)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))

    api(libs.spring.context)
    api(libs.spring.boot.autoconfigure)
    implementation(libs.kotlinlogging)
    implementation(libs.micrometer.statsd)
    implementation(project(":product-spring-modules:product-spring-module-base"))
    api(project(":product-spring-modules:product-spring-auth"))
    api(ronincommon.kafka)
    implementation("com.projectronin.product.audit.messaging:contract-messaging-audit:1.0.0")
    api(ronincommon.auth)
    implementation("io.projectreactor:reactor-core:3.5.9")

    kapt("org.springframework.boot:spring-boot-configuration-processor:" + libs.versions.springboot.get())

    testImplementation(libs.bundles.spring.test)
}

ktlint {
    filter {
        exclude("**/jsonschema/**")
    }
}
