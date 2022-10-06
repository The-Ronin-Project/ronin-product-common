import java.net.URL

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
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

val springDataTest by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.bundles.spring)
    implementation(libs.okhttp)

    testImplementation(libs.bundles.spring.test) {
        exclude(module = "mockito-core")
        exclude(module = "mockito-junit-jupiter")
    }
    testImplementation(libs.bundles.testcontainers)

    // Allows tests only use spring-data-jpa for the "springDataTest" task
    springDataTest(libs.bundles.spring.data)
}

tasks {
    register<Test>("springDataTest") {
        group = "verification"
        description = "Run tests with the spring-boot-data-jpa dependency"
        shouldRunAfter("test")
        classpath += springDataTest
    }

    check {
        dependsOn("springDataTest")
    }

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
                            "https://github.com/projectronin/ronin-product-common/blob/main/product-spring-common/src/main/kotlin/"
                        )
                    )
                    // Suffix which is used to append the line number to the URL. Use #L for GitHub
                    remoteLineSuffix.set("#L")
                }

                externalDocumentationLink {
                    url.set(URL("https://docs.spring.io/spring-boot/docs/" + libs.versions.springboot.get() + "/api/"))
                }

                externalDocumentationLink {
                    url.set(URL("https://docs.spring.io/spring-security/site/docs/current/api/"))
                }

                externalDocumentationLink {
                    url.set(URL("https://docs.spring.io/spring-framework/docs/current/javadoc-api/"))
                }

                externalDocumentationLink {
                    url.set(URL("https://fasterxml.github.io/jackson-databind/javadoc/2.13/"))
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
        register("spring-common", MavenPublication::class) {
            from(components["java"])
        }
    }
}

