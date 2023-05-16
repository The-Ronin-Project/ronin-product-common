import java.util.Properties
import kotlin.math.min

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

// Used to declare the project version.  Takes environment variables that are passed in by the snapshot_publish github workflow:
//
// REF_NAME: branch or tag name.
// REF_TYPE: "branch" or "tag"
//
// If these aren't passed in, assumes REF_TYPE == "branch" and tries to get the branch name and latest tag from the repository, use the tag
// name to determine semver version, and then increment the patch version.
//
// If they are passed in, the logic is:
// If REF_TYPE == "tag" tag and if tag is `vN.N.N`, create a release version of the project at that version number
// Otherwise, find the latest tag that matches `vN.N.N` and increment the patch version and add `-SNAPSHOT`.  If the branch
// isn't "main" then add a truncated version of the branch name to the version to keep them separate when there are multiple live PRs
val version: String = run {
    val refName: String = System.getenv("REF_NAME") ?: run {
        val proc = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
            .directory(rootProject.projectDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(1, TimeUnit.MINUTES)
        if (proc.exitValue() != 0) {
            throw RuntimeException("Couldn't get branch name:\nout:${proc.inputStream.bufferedReader().readText()}\nerr:${proc.errorStream.bufferedReader().readText()}")
        }
        proc.inputStream.bufferedReader().readText().trim()
    }
    val refType: String = System.getenv("REF_TYPE") ?: "branch"

    val versionMatcher = """v[0-9]+.[0-9]+.[0-9]+""".toRegex()

    val tagName = if (refType == "tag" && refName.matches(versionMatcher)) {
        refName
    } else {
        val proc = ProcessBuilder("git", "tag", "--list", "--sort=-v:refname")
            .directory(rootProject.projectDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(1, TimeUnit.MINUTES)
        if (proc.exitValue() != 0) {
            throw RuntimeException("Couldn't get tag list:\nout:${proc.inputStream.bufferedReader().readText()}\nerr:${proc.errorStream.bufferedReader().readText()}")
        }
        proc.inputStream.bufferedReader().readLines()
            .map { it.trim() }
            .firstOrNull { it.matches(versionMatcher) } ?: "v0.0.0"
    }

    val baseVersion = tagName.replace("v", "")

    if (refType == "tag") {
        baseVersion
    } else {
        val segments = baseVersion.split(".").map { it.toInt() }
        val refNameBase = refName
            .replace("^.*/".toRegex(), "")
            .replace("[^A-Za-z0-9]".toRegex(), "_")
        val refInfix = if (refNameBase == "main") {
            ""
        } else {
            "-${refNameBase
                .substring(0, min(10, refNameBase.length))
                .replace("_$".toRegex(), "")
                .uppercase()}"
        }
        "${segments[0]}.${segments[1]}.${segments[2] + 1}$refInfix-SNAPSHOT"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // This whole mess tries to supplement the TOML file by adding _this project's_ version to it dynamically,
            // and by recursing the project structure and declaring libraries for each module.  The primary problem is that
            // gradle plugins are messy, so it tries to guess the IDS using various kinds of file searches and string grepping.
            version("product-common", version)
            val basicGradlePluginPattern = "(.*)\\.gradle\\.kts".toRegex()
            val pluginIdReplacerPattern = "[^A-Za-z]".toRegex()
            val buildFileIdPattern = "(?ms).*gradlePlugin.*id *= *\"([^\"]+)\".*".toRegex()
            val pluginIdSegmentPattern = """com\.projectronin\.product\.(.+)""".toRegex()
            val groupId = "com.projectronin.product"

            fun recurseProjects(descriptor: ProjectDescriptor) {
                if (descriptor.parent?.name == "gradle-plugins") {

                    val pluginScripts = descriptor.projectDir.walk()
                        .filter { file -> file.path.contains("src/main/kotlin") && file.name.matches(basicGradlePluginPattern) }
                        .map { file ->
                            file.name.replace(basicGradlePluginPattern, "$1")
                        }
                        .toList()
                    if (pluginScripts.isNotEmpty()) {
                        pluginScripts
                            .forEach { idSegment ->
                                plugin("product-${idSegment.replace(pluginIdReplacerPattern, "")}", "$groupId.$idSegment").version(version)
                            }
                    } else {
                        val buildFileText = descriptor.buildFile.readText()
                        if (buildFileText.contains(buildFileIdPattern)) {
                            val pluginId = buildFileText.replace(buildFileIdPattern, "$1")
                            val segment = pluginId.replace(pluginIdSegmentPattern, "$1")
                            plugin("product-$segment", pluginId).version(version)
                        }
                    }
                } else if (descriptor.name != "gradle-plugins" && descriptor.name != "ronin-product-common") {
                    library(descriptor.name, groupId, descriptor.name).version(version)
                }
                descriptor.children.forEach { subProject ->
                    recurseProjects(subProject)
                }
            }

            recurseProjects(rootProject)

            // for backward compatibility
            library("product-starter-web", groupId, "product-spring-web-starter").version(version)
            library("product-starter-webflux", groupId, "product-spring-webflux-starter").version(version)
            library("product-contracttest", groupId, "product-contract-test-common").version(version)
            library("spring-productcommon", groupId, "product-spring-common").version(version)
        }
    }
}
