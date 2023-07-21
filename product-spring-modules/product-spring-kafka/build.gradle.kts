plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
    id(libs.plugins.kotlin.kapt.get().pluginId)
}

dependencies {
    implementation(libs.kotlinlogging)
    implementation(platform(libs.spring.boot.bom))
    api(libs.ronin.kafka)
    api(libs.spring.context)
    api(libs.spring.boot.autoconfigure)

    kapt("org.springframework.boot:spring-boot-configuration-processor:" + libs.versions.springboot.get())
}
