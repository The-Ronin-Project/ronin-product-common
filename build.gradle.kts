@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    `version-catalog`
    `maven-publish`
}

group = "com.projectronin"
version = libs.versions.product.common.get()

detekt {
    ignoreFailures = true
}

kover {
    engine.set(kotlinx.kover.api.DefaultJacocoEngine)
}

koverMerged {
    enable()

    xmlReport {
        reportFile.set(layout.buildDirectory.file("coverage/report.xml"))
    }

    htmlReport {
        reportDir.set(layout.buildDirectory.dir("coverage/html"))
    }
}

allprojects {
    repositories {
        maven {
            url = uri("https://repo.devops.projectronin.io/repository/maven-public/")
            mavenContent {
                releasesOnly()
            }
        }
    }
}

catalog {
    versionCatalog {
        from(files("./gradle/libs.versions.toml"))
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
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}
