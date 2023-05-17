import kotlin.math.min

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    `version-catalog`
    `maven-publish`
}

detekt {
    ignoreFailures = true
}

kover {
    engine.set(kotlinx.kover.api.DefaultIntellijEngine)
}

koverMerged {
    enable()

    xmlReport {
        reportFile.set(layout.buildDirectory.file("coverage/report.xml"))
        filters {
            classes {
                excludes += "com.projectronin.product.plugin.openapi.*"
                excludes += "com.projectronin.product.*Plugin"
                excludes += "com.projectronin.product.*_gradle*"
                excludes += "com.projectronin.product.common.testutils.*"
            }
        }
    }

    htmlReport {
        reportDir.set(layout.buildDirectory.dir("coverage/html"))
        filters {
            classes {
                excludes += "com.projectronin.product.plugin.openapi.*"
                excludes += "com.projectronin.product.*Plugin"
                excludes += "com.projectronin.product.*_gradle*"
                excludes += "com.projectronin.product.common.testutils.*"
            }
        }
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
val targetVersion: String = run {
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
            "-${
                refNameBase
                    .substring(0, min(10, refNameBase.length))
                    .replace("_$".toRegex(), "")
                    .uppercase()
            }"
        }
        "${segments[0]}.${segments[1]}.${segments[2] + 1}$refInfix-SNAPSHOT"
    }
}

allprojects {
    group = "com.projectronin"
    version = targetVersion

    repositories {
        maven {
            url = uri("https://repo.devops.projectronin.io/repository/maven-public/")
            mavenContent {
                releasesOnly()
            }
        }
    }
}

fun extractPlugins(currentProject: Project): List<Pair<String, String>> {
    val basicGradlePluginPattern = "(.*)\\.gradle\\.kts".toRegex()
    val pluginIdReplacerPattern = "[^A-Za-z]".toRegex()
    val buildFileIdPattern = "(?ms).*gradlePlugin.*id *= *\"([^\"]+)\".*".toRegex()
    val pluginIdSegmentPattern = """com\.projectronin\.product\.(.+)""".toRegex()

    val pluginScripts = currentProject.projectDir.walk()
        .filter { file -> file.path.contains("src/main/kotlin") && file.name.matches(basicGradlePluginPattern) }
        .map { file ->
            file.name.replace(basicGradlePluginPattern, "$1")
        }
        .toList()
    return if (pluginScripts.isNotEmpty()) {
        pluginScripts
            .map { idSegment ->
                "product-${idSegment.replace(pluginIdReplacerPattern, "")}" to "com.projectronin.product.$idSegment"
            }
    } else {
        val buildFileText = currentProject.buildFile.readText()
        if (buildFileText.contains(buildFileIdPattern)) {
            val pluginId = buildFileText.replace(buildFileIdPattern, "$1")
            val segment = pluginId.replace(pluginIdSegmentPattern, "$1")
            listOf("product-$segment" to pluginId)
        } else {
            logger.warn("Couldn't find plugin ID in ${currentProject.name}")
            emptyList()
        }
    }
}

catalog {
    versionCatalog {
        from(files("./gradle/libs.versions.toml"))
        // This whole mess tries to supplement the TOML file by adding _this project's_ version to it dynamically,
        // and by recursing the project structure and declaring libraries for each module.  The primary problem is that
        // gradle plugins are messy, so it tries to guess the IDS using various kinds of file searches and string grepping.
        version("product-common", targetVersion)

        fun handleProject(currentProject: Project) {
            if (currentProject.parent?.name == "gradle-plugins") {
                extractPlugins(currentProject)
                    .forEach { pluginPair ->
                        plugin(pluginPair.first, pluginPair.second).versionRef("product-common")
                    }
            } else {
                library(currentProject.name, currentProject.group.toString(), currentProject.name).versionRef("product-common")
            }
        }

        subprojects
            .forEach { handleProject(it) }

        // for backward compatibility
        library("product-starter-web", rootProject.group.toString(), "product-spring-web-starter").versionRef("product-common")
        library("product-starter-webflux", rootProject.group.toString(), "product-spring-webflux-starter").versionRef("product-common")
        library("product-contracttest", rootProject.group.toString(), "product-contract-test-common").versionRef("product-common")
        library("spring-productcommon", rootProject.group.toString(), "product-spring-common").versionRef("product-common")
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

tasks.create("printVersion") {
    doLast {
        logger.lifecycle(project.version.toString())
    }
}
