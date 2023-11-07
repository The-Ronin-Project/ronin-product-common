

rootProject.name = "ronin-product-common-root"

// Spring libraries
include(":product-spring-modules:product-spring-actuator")
include(":product-spring-modules:product-spring-audit")
include(":product-spring-modules:product-spring-auth")
include(":product-spring-modules:product-spring-auth:product-spring-auth-seki-client")
include(":product-spring-modules:product-spring-auth:product-spring-auth-seki-testutils")
include(":product-spring-modules:product-spring-auth:product-spring-seki-auth")
include(":product-spring-modules:product-spring-auth:product-spring-seki-auth:product-spring-seki-auth-webmvc")
include(":product-spring-modules:product-spring-auth:product-spring-seki-auth:product-spring-seki-auth-webflux")
include(":product-spring-modules:product-spring-auth:product-spring-auth-m2m-client")
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
include(":product-spring-modules:product-spring-kafka")
include(":product-spring-modules:product-spring-openapi-from-contract")
include(":product-spring-modules:product-spring-openapi-generated")
include(":product-spring-modules:product-spring-cors")
include(":product-spring-modules:product-spring-cors:product-spring-cors-webmvc")
include(":product-spring-modules:product-spring-cors:product-spring-cors-webflux")
include(":product-spring-modules:product-spring-kafka")
include(":product-spring-modules:product-spring-telemetry")
include(":product-spring-modules:product-spring-logging")
include(":product-spring-modules:product-spring-module-base")
include(":product-spring-modules:product-spring-launchdarkly")

include(":product-spring-modules:product-spring-common")
include(":product-spring-modules:product-spring-web-starter")
include(":product-spring-modules:product-spring-webflux-starter")

// Other libraries
include(":product-contract-test-common")

// catalog
include(":ronin-product-common-catalog")

findProject(":ronin-product-common-catalog")?.name = "ronin-product-common"

pluginManagement {
    repositories {
        maven {
            url = uri("https://repo.devops.projectronin.io/repository/maven-public/")
        }
        mavenLocal()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://repo.devops.projectronin.io/repository/maven-public/")
        }
        mavenLocal()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("roningradle") {
            from("com.projectronin.services.gradle:ronin-gradle-catalog:2.3.0")
        }
        create("ronincommon") {
            from("com.projectronin:ronin-common:2.0.9")
        }
    }
}
