# Contact Testing Utilities

Works with the [local-ct](../product-gradle-common) plugin.

Meant to be used to set up simple contract tests of a running service locally, using docker-compose.

To set up, do the following:

In your service's build.gradle.kts file, add:

```kotlin
plugins {
    id(libs.plugins.product.localContractTest.get().pluginId)
}

dependencies {
    localContractTestImplementation(libs.product.contracttest)
}
```

Add a docker-compose.yml to your project.  It needs a couple of special things:

It can't use `container_name: XYZ` names because it interferes with being able to address the containers by name.

To manage dependent service contracts (e.g. seki), it needs a container like:
```yaml
  wiremock:
    profiles:
      - wiremock
    image: wiremock/wiremock:2.35.0
```

To change the configured downstream endpoints for dependent services (e.g. seki), it needs some specific environment settings:

```yaml
  service:
    env_file:
      - default.env
    build:
        context: student-data-service
        dockerfile: Dockerfile
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db/db
      - SPRING_DATASOURCE_USERNAME=db_user
      - SPRING_DATASOURCE_PASSWORD=db_pass
      - SEKI_URL
    ports:
      - 8080:8080
```

Note the reference to a `default.env`, and note that the variable we want to override / change for local integration testing
is specified without a value (e.g. SEKI_URL).

Then create two env files:

default.env:
```
SEKI_URL=https://seki.dev.projectronin.io/
```

wiremock.env:
```
SEKI_URL=http://wiremock:8080/seki
```

You can of course do this for multiple environment variables.

Create a `src/localContractTest` directories containing a `kotlin` and a `resouces` directory.

Then `src/localContractTest/resources`, create two files:

logback-test.xml:
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="STDOUT"/>
    </root>

    <logger name="org.testcontainers" level="INFO"/>
    <logger name="com.github.dockerjava" level="WARN"/>
    <logger name="com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.wire" level="OFF"/>
</configuration>
```

test.properties:
```properties
project.root=${projectRoot}
```

Note that the project.root directory should point to the location where the docker-compose.yml and wiremock.env are stored.  The `${projectRoot}`
will be replaced by gradle on build to point to the root of the parent project, but you can add directories like:

test.properties:
```properties
project.root=${projectRoot}/my-service-directory
```

if that's where you store your docker compose and env file.

Then you can create tests.  See [DockerExtensionTest](./src/test/kotlin/com/projectronin/product/contracttest/DockerExtensionTest.kt) for
a fully worked example. The annotation:

```kotlin
@ExtendWith(DockerExtension::class)
```

manages the startup and shutdown of the docker compose before and after the entire suite.
