import java.net.URL

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
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

kover {
    engine.set(kotlinx.kover.api.DefaultIntellijEngine)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.bundles.spring.webflux)
    api(project(":product-spring-modules:product-spring-auth"))
    api(project(":product-spring-modules:product-spring-auth:product-spring-seki-auth"))

    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.assertj)
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

    dokkaHtmlPartial {
        dokkaSourceSets {
            configureEach {
                sourceLink {
                    // Unix based directory relative path to the root of the project (where you execute gradle respectively).
                    localDirectory.set(file("src/main/kotlin"))
                    // URL showing where the source code can be accessed through the web browser
                    remoteUrl.set(
                        URL(
                            "https://github.com/projectronin/ronin-product-common/blob/main/product-contract-test-common/src/main/kotlin/"
                        )
                    )
                    // Suffix which is used to append the line number to the URL. Use #L for GitHub
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useJUnitJupiter()
            targets {
                all {
                    testTask.configure {
                        testLogging {
                            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                            events(org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED)
                        }
                    }
                }
            }
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
        register("contract-test-common", MavenPublication::class) {
            from(components["java"])
        }
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    disabledRules.set(setOf("no-wildcard-imports"))
}
