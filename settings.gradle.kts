rootProject.name = "ronin-product-common"

// Gradle plugins
include(":gradle-plugins")
include(":gradle-plugins:product-gradle-jvm")
include(":gradle-plugins:product-gradle-openapi")
include(":gradle-plugins:product-gradle-spring")
include(":gradle-plugins:product-gradle-json-schema")
include(":gradle-plugins:product-gradle-local-it")

// Spring libraries
include(":product-spring-common")
include(":product-spring-web-starter")
include(":product-spring-webflux-starter")

// Other libraries
include("product-contract-test-common")

pluginManagement {
    repositories {
        maven {
            url = uri("https://repo.devops.projectronin.io/repository/maven-snapshots/")
            mavenContent {
                snapshotsOnly()
            }
        }
        maven {
            url = uri("https://repo.devops.projectronin.io/repository/maven-releases/")
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            url = uri("https://repo.devops.projectronin.io/repository/maven-public/")
            mavenContent {
                releasesOnly()
            }
        }
        mavenLocal()
        gradlePluginPortal()
    }
}
