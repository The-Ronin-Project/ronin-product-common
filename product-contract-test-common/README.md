# Contact Testing Utilities

Works with the [local-ct](../gradle-plugins/product-gradle-local-ct) plugin.

Meant to be used to set up simple contract tests of a running service locally.

To set up, do the following:

In your service's build.gradle.kts file, add:

```kotlin
plugins {
    alias(libs.plugins.product.localct)
}

dependencies {
    localContractTestImplementation(libs.product.contracttest)
}
```

Create a `src/localContractTest` directories containing a `kotlin` and a `resouces` directory.

Then `src/localContractTest/resources`, create three files:

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
    <logger name="YOUR PACKAGE HERE" level="DESIRED LEVEL"/>
    <logger name="com.github.dockerjava" level="WARN"/>
    <logger name="com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.wire" level="OFF"/>
</configuration>

```

test.properties:
```properties
project.build.dir=${projectBuild}
project.dir=${projectDir}
```

application-test.properties:
```properties
spring.datasource.url={{mySqlJdbcUri}}
spring.liquibase.enabled=true

seki.url=http://localhost:{{wireMockPort}}/seki

ronin.kafka.bootstrapServers={{bootstrapServers}}
ronin.kafka.security-protocol=PLAINTEXT
```

Include in the application-test.properties any URLs that need to be pointed to wiremock.  Obviously, if you don't have a database or Kafka, leave them out.

In `src/localContractTest/kotlin/your/test/package`, create a class that implements `com.projectronin.product.contracttest.services.ContractServicesProvider`.  The
`provideServices()` function will need to return a list of `com.projectronin.product.contracttest.services.ContractTestService` implementations that need to be started.
You can look at the existing implementations in `com.projectronin.product.contracttest.services` as an example of those services.  There are pre-built ones for MySql, wiremock,
and running a spring-boot jar.  You can see `com.projectronin.product.contracttest.StudentDataServicesProvider` for how you might use these, but see the comments in that
file for pieces that are very specific to testing in _this_ project.

Then you can create tests. See [LocalContractTestExtensionTest](./src/test/kotlin/com/projectronin/product/contracttest/LocalContractTestExtensionTest.kt) for
a fully worked example. The annotation:

```kotlin
@ExtendWith(LocalContractTestExtension::class)
```

manages the startup and shutdown of the docker compose before and after the entire suite; add it to all of the contract tests you write.

## Kafka Topics

The Kafka instance does not have automatic topic creation enabled when connecting with Streams. You can specify topics
when defining the Test Kafka Service by passing in topics from the same namespace with optional partition and replicas.

```kotlin
ContractTestKafkaService(
    Topic("topic-name-a"),
    Topic(name = "topic-name-b", partitions = 2, replicas = 1)
)
```
