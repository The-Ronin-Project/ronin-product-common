# Product Spring Audit
Library used to write messages to Kafka to get recorded to the Audit Service.

## Adding to a project
First the service needs Write access to the audit topic. If this is the first access for the service to Kafka, this will also include setting 
up the groups, user, password, ACL's. PlatEng can help with this. Just make sure to request write access to the audit command topic, `oci.us-phoenix-1.ronin-audit.command.v1`.

### settings.gradle.kts
Make sure you are on a new enough version of ronin-product-common. Greater than 2.3.2
```
    versionCatalogs {
        create("libs") {
            from("com.projectronin:ronin-product-common:2.4.6")
        }
    }
```
### build.gradle.kts
Include the dependency. Here we use api so we don't need to pull each kafka dependency in separately. But you do you and either use api, or pull in the required 
Kafka libraries as well.
```
    api(libs.product.spring.audit)
```

### SharedConfigurationReference
Need to make sure that the proper config classes are included. You can trust the magic that is ComponentScna, or directly Import the following two configs.
`AuditConfig` sets up the bean Auditor and pulls in the require configuration. `KafkaConfiguration` pulls in the Kafka Cluster properties from a common location for any 
aspects of the service to use.
```
@Import(AuditConfig::class, KafkaConfiguration::class)
```

### Full Configuration (application.yml, ENV from charts)
```yaml
# 
ronin:
  kafka:
    bootstrap-servers: [server addresses]       # Comes from charts in OCI, set in local to use kafka locally
    security-protocol: [broker security]        # Clusters in OCI use SASL_SSL (Default), locally set to "PLAINTEXT"
    saslMechanism: [broker security]            # Defaults to "SCRAM-SHA-512" for OCI clusters. Not needed for local
    saslUsername: [broker security]             # Needs to be set from configs injected from helm charts. Not needed for local
    saslPassword: [broker security]             # Needs to be set from Vault and vals operator from helm charts. Not needed for local
    saslJaasConfig: [broker security]           # Calculated for OCI from username and password. Not needed for local
    
  product:
    audit:
      sourceService: [service name]             # Set in application.yml to official service name
      topic:  [audit command topic]             # Defaults to the correct topic and shouldn't need to be set
      enabled: true                             # Defaults to true. When true it does not attempt to connect to Kafka so use locally

```

### TLDR Config
Minimal Dev/Stage/Prod application.yml
```yaml
ronin:
  kafka:
    saslUsername: [service defined by plateng]
```

Helm values file
```yaml
    - name: RONIN_KAFKA_BOOTSTRAP_SERVERS
      valueFrom:
        configMapKeyRef:
          name: [service]-config
          key: KAFKA_BOOTSTRAP_SERVERS
```

Helm environment values file (dev-values.yml, stage-values.yml, prod-values.yml)
```yaml
environment: dev
...
configMap:
  ...
  data:
    KAFKA_BOOTSTRAP_SERVERS: kafka-01.dev.us-phoenix-1.oci.projectronin.cloud:9092,kafka-02.dev.us-phoenix-1.oci.projectronin.cloud:9092,kafka-03.dev.us-phoenix-1.oci.projectronin.cloud:9092
...
valssecret:
  enabled: true
  data:
    RONIN_KAFKA_SASL_PASSWORD:
      ref: ref+vault://product_engineering/[service vault location]/dev/KAFKA_SASL_PASSWORD
```

## Usage
Usage of the auditor is fairly simple. It is designed to be able to add to the controllers simply by injecting the Auditor bean. Then from there, just drop
audit calls in where needed.

```kotlin
private val auditor: Auditor

// Writes an Audit entry for a CREATE action on data. 
auditor.create("Patient", "Asset", asset.metadata.id.toString(), mapOf("patientId" to patientId))

// There is also READ, DELETE, UPDATE
auditor.read(...)
auditor.update(...)
auditor.delete(...)

```
Message Example:
```json
{
	"tenantId": "apposnd",
	"userId": "3e066064-b1b5-4560-ba0e-5bfba57cad9c",
	"userFirstName": "Carl",
	"userLastName": "Graving",
	"userFullName": "Carl Graving",
	"resourceCategory": "Patient",
	"resourceType": "Asset",
	"resourceId": "306f80a4-10f9-4937-951b-45383a899208",
	"mrn": "",
	"action": "READ",
	"reportedAt": "2023-05-12T20:26:07.788780332Z",
	"dataMap": [
		"patientId:patient1234"
	]
}
```

## Notes / FAQs

* When running locally, with the Kafka cluster disabled the auditor will log the following type of messages:
`11:01:44.965 [Test worker] INFO com.projectronin.product.audit.Auditor -- Audit Entry not written since Kafka is disabled by ronin.kafka.disabled configuration`


