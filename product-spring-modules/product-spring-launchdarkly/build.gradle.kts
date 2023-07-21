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
    implementation(libs.bundles.jakarta)
    implementation(libs.launchdarkly.sdk)

    kapt("org.springframework.boot:spring-boot-configuration-processor:" + libs.versions.springboot.get())

    testImplementation(libs.bundles.spring.test)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    disabledRules.set(setOf("no-wildcard-imports"))
}
