plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    `java-library`
}

group = rootProject.group
version = libs.versions.product.common.get()

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

java {
    withSourcesJar()
    withJavadocJar()
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
        register("spring-common", MavenPublication::class) {
            from(components["java"])
        }
    }
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.web)
    implementation(libs.spring.boot.security)
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.boot.validation)
    implementation(libs.spring.boot.data.jpa)
}
