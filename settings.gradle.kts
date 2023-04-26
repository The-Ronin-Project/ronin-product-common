rootProject.name = "ronin-product-common"

// Gradle plugins
include(":gradle-plugins")
include(":gradle-plugins:product-gradle-jvm")
include(":gradle-plugins:product-gradle-openapi")
include(":gradle-plugins:product-gradle-spring")
include(":gradle-plugins:product-gradle-json-schema")
include(":gradle-plugins:product-gradle-local-ct")

// Spring libraries
include(":product-spring-modules")
include(":product-spring-modules:product-spring-actuator")
include(":product-spring-modules:product-spring-auth")
include("product-spring-modules:product-spring-auth:product-spring-auth-seki-client")
include("product-spring-modules:product-spring-auth:product-spring-auth-seki-testutils")
include(":product-spring-modules:product-spring-auth:product-spring-seki-auth")
include(":product-spring-modules:product-spring-auth:product-spring-seki-auth:product-spring-seki-auth-webmvc")
include(":product-spring-modules:product-spring-auth:product-spring-seki-auth:product-spring-seki-auth-webflux")
include(":product-spring-modules:product-spring-auth:product-spring-jwt-auth-testutils")
include(":product-spring-modules:product-spring-auth:product-spring-jwt-auth")
include(":product-spring-modules:product-spring-auth:product-spring-jwt-auth:product-spring-jwt-auth-webmvc")
include(":product-spring-modules:product-spring-auth:product-spring-jwt-auth:product-spring-jwt-auth-webflux")
include(":product-spring-modules:product-spring-auth:product-spring-jwt-auth:product-spring-jwt-auth-mocks")
include(":product-spring-modules:product-spring-exceptionhandling")
include(":product-spring-modules:product-spring-exceptionhandling:product-spring-exceptionhandling-webmvc")
include(":product-spring-modules:product-spring-exceptionhandling:product-spring-exceptionhandling-webflux")
include(":product-spring-modules:product-spring-fhir")
include(":product-spring-modules:product-spring-httpclient")
include(":product-spring-modules:product-spring-jackson")
include(":product-spring-modules:product-spring-jackson:product-spring-jackson-web")
include(":product-spring-modules:product-spring-openapi-from-contract")
include(":product-spring-modules:product-spring-openapi-generated")
include(":product-spring-modules:product-spring-cors")
include(":product-spring-modules:product-spring-cors:product-spring-cors-webmvc")
include(":product-spring-modules:product-spring-cors:product-spring-cors-webflux")

include(":product-spring-modules:product-spring-common")
include(":product-spring-modules:product-spring-web-starter")
include(":product-spring-modules:product-spring-webflux-starter")

// Other libraries
include(":product-contract-test-common")

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
