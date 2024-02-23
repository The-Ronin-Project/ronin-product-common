# A Spring Resource-Server based oauth authentication library for Kotlin Services

This module and its children implement a JWT-based OAuth2 resource server auth setup for our kotlin services.  It is intended to replace [product-spring-seki-auth](../product-spring-seki-auth).
Ideally it should require less custom code, be more standards-based, and should handle more options than the original Seki setup.  It should be able to support multiple auth servers (Auth0 and
prodeng-auth-service for instance), while at the same time supporting Seki for backward compatibility.

## Controller Unit Tests

In your controller unit tests, you can generate the tokens you want like this:

First, import the auth mocks module to your service project:

```kotlin
dependencies {
    testImplementation(productcommon.product.spring.jwt.auth.mocks)
}
```

Then, Enable mock authentication by including JwtAuthMockConfig in your test configuration, like this:

```kotlin
@WebMvcTest(ControllerUnderTest::class)
@Import(SharedConfigurationReference::class, JwtAuthMockConfig::class) // include mock config object
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ControllerUnderTestIntegrationTest(
    @Autowired val mockMvc: MockMvc
) {
    // test some stuff
}
```

When you are testing, you can use two basic mechanisms.  If all you care about is that "you have a token" and it's ok that that token be a provider token in the `apposnd` tenant,
you can do this:

```kotlin
import com.projectronin.product.common.testutils.getDefaultToken

@Test
fun `should just accept the token`() {
    mockMvc.perform(
        MockMvcRequestBuilders.post(SOME_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer $defaultToken"
            )
            .content("""{}""")
    )
        .andExpect(MockMvcResultMatchers.status().isOk)
}
```

But in many cases you will want to customize that token.  You can do so like this:

```kotlin
@Test
fun `should succeed with the admin:read scope`() {
    withMockJwtAuth {
        withJwtAuthToken {
            withScopes("admin:read")   // whatever you do here persists until the end of `withMockJwtAuth{}` or until you use withJwtAuthToken again
        }
        mockMvc.perform(
            get(SOME_PATH)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer $token" // note here that the token is provided by withMockJwtAuth
                )
                .addDefaultParams()
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
}
```

You probably want to make sure that failed auth also works.  You can do that by making the token throw an exception:

```kotlin
@Test
fun `should fail if unauthorized`() {
    withMockJwtAuth {
        withJwtAuthToken(InvalidBearerTokenException("An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"))
        mockMvc.perform(
            MockMvcRequestBuilders.post(SOME_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer $token" // note here that the token is provided by withMockJwtAuth
                )
                .content("""{}""")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.httpStatus").value("UNAUTHORIZED"))
            .andExpect(MockMvcResultMatchers.jsonPath("\$.detail").value("An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"))
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()
    }
}
```

There are a lot of options for the token; see all the methods available in RoninTokenBuilderContext [here](https://github.com/projectronin/ronin-common/blob/main/test-utilities/jwt-auth-test/src/main/kotlin/com/projectronin/test/jwt/AuthTokenHelpers.kt)

## Local Contract Tests

See brief README [here](https://github.com/projectronin/ronin-common/blob/main/test-utilities/jwt-auth-test/README.md) and see code documentation in that module

## Domain Integration Tests

See brief README [here](https://github.com/projectronin/ronin-common/blob/main/test-utilities/jwt-auth-test/README.md) and see code documentation in that module

## Changes

Or "why have this new auth anyway?"

Spring has started supplying a fully-configured OAuth2 resource server auth configuration capability as part for spring security, including full abilities to decode and validate JWTs.  Since
it's better to rely on a tested implementation, this module and its children use that "spring resource server" setup to build an auth configuration that still supports Seki.

So other than the underlying code, this provides capabilities like the following:

- Fully support parallel usage of Seki, "Seki Replacement", and external OAuth2 authorization servers (like Auth0)
- Validate tokens via signature locally
- Enrich the RoninAuthentication objects returned with more stable and useful data, including scopes and better user metadata.
- Help remove references to seki from the services
- Allow configuration of the secured and unsecured endpoints

## Configuration

Configuration is done through [JwtSecurityProperties.kt](src/main/kotlin/com/projectronin/product/common/config/JwtSecurityProperties.kt).

For example, your service could provide an application.yml similar to the following:

```yaml
ronin:
  auth:
    issuers:
      # for auth0 M2M auth
      - "https://ronin-dev.auth0.com/"
      # for new auth2 service
      - "https://dev.projectronion.io/auth/"
      # to support seki
      - "Seki"
    detailed-errors: false
    secured-path-patterns:
     - "/api/**"
```

If you are using the Seki issuer, you have to supply a `seki.url` property.

If you want seki tokens validated by signature before calling seki validate, you would also need ronin-charts + vault to supply:

```bash
RONIN_AUTH_SEKI_SHARED_SECRET=Y6mvgnKzGr8j9TsU8Zt7uU4b1wmFgCSnGoGkIGBpd4NKwRYZVWiLQfRZczupQ3zETsxlaVDMsjneTxi8eVorVwFxjWkChp3pUIfg
```

You can leave off all of the seki-related properties (including `seki.url`) and your service will support only the other issuers, and ignore Seki entirely.

## Authorization

You can annotate your endpoints with standard Spring `@PreAuthorize` annotations like this:

```kotlin
    @GetMapping("/object/{tenant-id}/by/tenant", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("#tenantId == authentication.tenantId")
    open fun getSampleObjectByTenant(@PathVariable("tenant-id", required = true) tenantId: String): ResponseEntity<Any> {
        return ResponseEntity
            .ok("""{"tenantId": "$tenantId"}""")
    }

    @GetMapping("/object-requiring-role", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('SCOPE_thing_requiring_scope:read')")
    open fun getSampleObjectRequiringRole(): ResponseEntity<Any> {
        return ResponseEntity
            .ok("""{"foo": null}""")
    }
```

We're trying to add some "meta-annotations" for common auth scenarios like [PreAuthPatient.kt](src%2Fmain%2Fkotlin%2Fcom%2Fprojectronin%2Fproduct%2Fcommon%2Fauth%2Fannotations%2FPreAuthPatient.kt).

You can _also_ accept the authentication instance into your method and write code to handle it:

```kotlin
    @GetMapping("/sample-object", produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getSampleObject(authentication: RoninAuthentication): ResponseEntity<Any>
```
