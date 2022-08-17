plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    `version-catalog`
    `maven-publish`
}

group = "com.projectronin"
version = libs.versions.product.common.get()

detekt {
    ignoreFailures = true
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
