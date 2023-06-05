@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.kapt)
    `maven-publish`
    `java-library`
}


kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

kover {
    engine.set(kotlinx.kover.api.DefaultIntellijEngine)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    api(project(":product-spring-modules:product-spring-actuator"))
    api(project(":product-spring-modules:product-spring-auth:product-spring-jwt-auth:product-spring-jwt-auth-webflux"))
    api(project(":product-spring-modules:product-spring-cors:product-spring-cors-webflux"))
    api(project(":product-spring-modules:product-spring-exceptionhandling"))
    api(project(":product-spring-modules:product-spring-exceptionhandling:product-spring-exceptionhandling-webflux"))
    api(project(":product-spring-modules:product-spring-fhir"))
    api(project(":product-spring-modules:product-spring-jackson:product-spring-jackson-web"))
    api(project(":product-spring-modules:product-spring-httpclient"))
    api(project(":product-spring-modules:product-spring-openapi-generated"))
    api(project(":product-spring-modules:product-spring-telemetry"))
    api(project(":product-spring-modules:product-spring-logging"))

    api(libs.bundles.spring.webflux)
    api(libs.okhttp)

    // Not in the bom for some reason, so we have to do this manual configuration
    kapt("org.springframework.boot:spring-boot-configuration-processor:" + libs.versions.springboot.get())

    testImplementation(libs.bundles.spring.test) {
        exclude(module = "mockito-core")
        exclude(module = "mockito-junit-jupiter")
    }
    testImplementation(libs.bundles.testcontainers)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "nexus"
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_TOKEN")
            }
            url = if (project.version.toString().endsWith("SNAPSHOT")) {
                uri("https://repo.devops.projectronin.io/repository/maven-snapshots/")
            } else {
                uri("https://repo.devops.projectronin.io/repository/maven-releases/")
            }
        }
    }

    publications {
        register("spring-webflux-starter", MavenPublication::class) {
            from(components["java"])
        }
    }
}
