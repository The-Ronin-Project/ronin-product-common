# Product Spring Tenant Stream
Library used to subscribe to Kafka events for the Tenant Service.

## Adding to a project
The service needs read access to the tenant event topic `oci.us-phoenix-1.ronin-tenant.tenant.v1`. If this is the first access for the service to Kafka, this will also include setting
up the groups, user, password, ACL's. PlatEng can help with this.

### settings.gradle.kts
Make sure you are on a new enough version of ronin-product-common. Greater than 2.9.25
```
    versionCatalogs {
        create("productcommon") {
            from("com.projectronin:ronin-product-common:2.9.25")
        }
    }
```
### build.gradle.kts
Include the dependency. The Tenant message schema is included with this.
```
    implementation(productcommon.product.spring.tenant.stream)
```

### SharedConfigurationReference
Need to make sure that the proper config classes are included. You can trust the magic that is ComponentScan, or directly Import the following two configs.
`TenantStreamConfiguration` sets up the bean TenantEventStream and pulls in the require configuration. `KafkaConfiguration` pulls in the Kafka Cluster properties from a common location for any
aspects of the service to use.
```
@Import(TenantStreamConfiguration::class, KafkaConfiguration::class)
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
    tenant:
      applicationId: [stream application id]    # Sets the stream application id
      tenantTopic:  [tenant event topic]        # Defaults to the correct topic and shouldn't need to be set
      dlqTopic: [dlq topic]                     # The topic for sending failed messages

```
## Usage
To use the Tenant Stream, you need to create a event handler that implements the `TenantEventHandler` interface. Once you
have that implementation, create a bean for it in your application.

```kotlin
class TenantEventHandlerImpl : TenantEventHandler {
    override fun create(data: TenantV1Schema) {
        // handle creates
    }

    override fun update(data: TenantV1Schema) {
        // handle updates
    }

    override fun delete(data: TenantId) {
        // handle deletes
    }
}
```

```kotlin
    @Bean
    fun tenantEventHandler(): TenantEventHandler {
        return TenantEventHandlerImpl()
    }
```

Note that your handler should throw exceptions for situations it can't handle. Those messages will be sent to the DLQ topic
to be handled later.
