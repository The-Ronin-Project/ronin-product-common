plugins {
    alias(libs.plugins.kotlin.jvm)
    `version-catalog`
    `maven-publish`
}

group = "com.projectronin"
version = libs.versions.product.common.get()

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
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}
