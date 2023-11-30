plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    api(libs.spring.context)
    api(libs.spring.boot.autoconfigure)
    api(libs.springdoc.ui.common)
}

testing {
    suites {
        val webMVCTest by registering(JvmTestSuite::class) {
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

            dependencies {
                implementation(project())
                implementation(platform((libs.spring.boot.bom.get().toString())))
                implementation(libs.spring.boot.core)
                implementation(libs.spring.boot.autoconfigure)
                implementation(libs.spring.boot.web)
                libs.bundles.spring.web.get().forEach { dep ->
                    implementation(dep)
                }
                libs.bundles.spring.test.get().forEach { dep ->
                    implementation(dep)
                }
                implementation(libs.assertj)
            }

            testType.set("webmvc-test")
        }
        val webFluxTest by registering(JvmTestSuite::class) {
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

            dependencies {
                implementation(project())
                implementation(platform((libs.spring.boot.bom.get().toString())))
                implementation(libs.spring.boot.core)
                implementation(libs.spring.boot.autoconfigure)
                implementation(libs.spring.boot.webflux)
                libs.bundles.spring.webflux.get().forEach { dep ->
                    implementation(dep)
                }
                libs.bundles.spring.test.get().forEach { dep ->
                    implementation(dep)
                }
                implementation(libs.assertj)
            }

            testType.set("webflux-test")
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("webFluxTest"), testing.suites.named("webMVCTest"))
}
