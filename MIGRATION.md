# Migration from 2.5.x to 2.6.x

## Upgrade gradle

Execute:

```./gradlew wrapper --gradle-version 8.2```

Verify it works with:

```./gradlew build```

## Update settings repository and catalog references

Open `settings.gradle.com`, and delete the current `dependencyResolutionManagement` and `pluginManagement` sections.  Replace them with:

```kotlin
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
            from("com.projectronin.services.gradle:ronin-gradle-catalog:<VERSION>")
        }
        create("libs") {
            from("com.projectronin:ronin-product-common:<VERSION>")
        }
    }
}
```

## Update plugin references

Update any `libs.plugins.product.XYZ` plugin references in your project to the equivalent `roningradle.plugins` versions.  The names are close to the same:

* `libs.plugins.product.json.schema` -> `roningradle.plugins.jsonschema`
* `libs.plugins.product.jvm` -> `roningradle.plugins.buildconventions.kotlin.jvm`
* `libs.plugins.product.local.ct`-> `roningradle.plugins.localct`
* `libs.plugins.product.openapi`-> `roningradle.plugins.openapi`
* `libs.plugins.product.spring` -> `roningradle.plugins.buildconventions.spring.service`

## Update libs references

Most `libs.xyz` references won't change, but you might need to do some.  For example:

* `libs.product.contracttest` -> `libs.product.contract.test.common`

## Remove stuff that is no longer needed or doesn't work anymore or is no longer necessary

Sections that need removing from your build files include:

* sonar
* repositories
* koverMerged

You can _probably_ reduce your root project's build file to contain only:

```kotlin
plugins {
    alias(roningradle.plugins.buildconventions.root)
}
```

You can _probably_ reduce your database module's build file to contain only:

```kotlin
plugins {
    alias(roningradle.plugins.buildconventions.spring.database)
}
```

In your service build file, you should be able to replace this:

```kotlin
    alias(libs.plugins.product.jvm)
    alias(libs.plugins.product.spring)
    id(libs.plugins.kotlin.kapt.get().pluginId)
```

with

```kotlin
    alias(roningradle.plugins.buildconventions.spring.service)
```

If you have other modules that produce libraries you use internally for your service, you should be able to use `roningradle.buildconventions.kotlin.jvm` or
`roningradle.buildconventions.kotlin.library` (depending on if you want library semantics or not).

You should _not_ need to apply the `java` or `kotlin("jvm")` plugins independently; same with sonar, or code coverage tools.  The build conventions plugins should take care of that.

The `buildconventions` plugins provide lots of configuration.  You may no longer need any configurations like:

```kotlin
tasks {
    withType<Test> { }
}
```

for instance.

## For OpenAPI plugin

Configuration / extension package names have changed

* `com.projectronin.product.plugin.openapi.OpenApiKotlinGeneratorExtension` -> `com.projectronin.openapi.OpenApiKotlinGeneratorExtension`
* `com.projectronin.product.plugin.openapi.OpenApiKotlinGeneratorInputSpec` -> `com.projectronin.openapi.OpenApiKotlinGeneratorInputSpec`

## Update CI/CD

Replace the `test` job in .github/workflows/cicd.yml with:

```yaml
  test:
    runs-on: self-hosted
    steps:
      - name: test and build
        uses: projectronin/github/.github/actions/basic-gradle-build-publish@feature/add-composit-gradle-build-action
        with:
          nexus_user: ${{ secrets.NEXUS_MAVEN_USER }}
          nexus_token: ${{ secrets.NEXUS_MAVEN_TOKEN }}
          github_token: ${{ secrets.GITHUB_TOKEN }}
          sonar_token: ${{ secrets.SONAR_TOKEN }}
          sonar_host_url: ${{ secrets.SONAR_HOST_URL }}
          codecov_token: ${{ secrets.CODECOV_TOKEN }}
          publish: false
```
