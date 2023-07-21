plugins {
    alias(roningradle.plugins.buildconventions.kotlin.library)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    api(project(":product-spring-modules:product-spring-jackson"))
    api(libs.spring.context)
    api(libs.spring.boot.autoconfigure)
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
                libs.bundles.spring.web.get().forEach { dep ->
                    implementation(dep)
                }
                libs.bundles.spring.test.get().forEach { dep ->
                    implementation(dep)
                }
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
                libs.bundles.spring.webflux.get().forEach { dep ->
                    implementation(dep)
                }
                libs.bundles.spring.test.get().forEach { dep ->
                    implementation(dep)
                }
            }

            testType.set("webflux-test")
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("webFluxTest"), testing.suites.named("webMVCTest"))
}
