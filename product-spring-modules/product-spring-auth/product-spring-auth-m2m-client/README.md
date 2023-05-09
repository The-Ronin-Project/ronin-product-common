# Simple M2M Token Client

This module is a simple client for fetching machine-to-machine JWT tokens from auth0.  It's pretty straightforward.  The main entry point is
[M2MClientService.kt](src/main/kotlin/com/projectronin/product/common/auth/m2m/M2MClientService.kt), which details its basic use.

This compliments [product-spring-jwt-auth](../product-spring-jwt-auth), as that module enables a service to easily consume, validate, and utilize M2M tokens without further changes
to the security filters.

Generally, in a spring boot application, you would use it be creating a configuration that instantiates a bean of M2MClientService type:

```kotlin
@AutoConfiguration
open class M2MConfiguration {
    
    @Bean
    open fun m2mClientService(
        httpClient: OkHttpClient,
        objectMapper: ObjectMapper,
        @Value("\${auth.m2m.url}") auth0Url: String,
        @Value("\${auth.m2m.clientId}") clientId: String,
        @Value("\${auth.m2m.clientSecret}") clientSecret: String,
    ) = M2MClientService(
        httpClient,
        objectMapper,
        auth0Url,
        clientId,
        clientSecret
    )
}
```

You can then inject the resulting bean any place you need to use an M2M token to access another service.  It's worth noting that if you already have a provider or patient token
in your service call, you generally will want to pass that token along instead.  M2M tokens should be primarily used in two cases:

- When calls are driven by asynchronous processes outside of the context of a patient or provider session (e.g. processing FHIR events via Kafka or something similar)
- For integration tests in place of a "long-lived-seki-token"

To use it, before you make a call to the target service, obtain an M2M token:

```kotlin
val token = m2mClientService.getToken(
    audience = "https://some-service.ronin.io/"
)
// then make your call to the destination service
```

Token caching and expiration is handled by the M2MClientService service itself, so tokens with the same input parameters will be re-used until they are about to expire, and a
new one will be retrieved on the next call.

Read the documentation in [M2MClientService.kt](src/main/kotlin/com/projectronin/product/common/auth/m2m/M2MClientService.kt), specifically on the `getToken` call for more information
on parameter options and usage.
