# A Spring Resource-Server based oauth authentication library for Kotlin Services

This module and its children implement a JWT-based OAuth2 resource server auth setup for our kotlin services.  It is intended to replace [product-spring-seki-auth](../product-spring-seki-auth).
Ideally it should require less custom code, be more standards-based, and should handle more options than the original Seki setup.  It should be able to support multiple auth servers (Auth0 and
prodeng-auth-service for instance), while at the same time supporting Seki for backward compatibility.

## Updating from Seki Auth

To replace the existing Seki setup in your service, in theory, if you are using the default `implementation(libs.product.starter.web)` project, when you update to a version of
this project past 2.2.0, you will get this new updated JWT-based auth library instead of Seki auth.

In theory, no further configuration is needed.  This library should validate against Seki exactly as before, as long as you haven't supplied any modified Seki configuration or client usage.
The `seki.url` property (or `SEKI_URL` environment variable) will be read and used to configure the seki client, and Seki validation _should_ occur exactly as before.

THe biggest change you're likely to see in tests.  Instead of mocking the seki client, you'll need to target your spring test auth mocks at new objects, and you'll have to provide
something that looks like a real JWT.

In other words, you might have had a test that looks like this:

```kotlin
@WebMvcTest(ControllerUnderTest::class)
@Import(SharedConfigurationReference::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ControllerUnderTestIntegrationTest(
    @Autowired val mockMvc: MockMvc
) {
    companion object {
        // ...

        private const val DEFAULT_AUTH_VALUE = "Bearer my_token"

        private val DEFAULT_AUTH_RESPONSE = AuthResponse(
            // ...
        )
        // ...
    }

    @MockkBean
    lateinit var someMockService: SomeMockService

    @MockkBean
    lateinit var sekiClient: SekiClient

    @BeforeEach
    fun setup() {
        clearAllMocks() // must ensure mocks in clean state at beginning of each test.
    }

    // ...
    @Test
    fun testValidRequest() {
        val patientPostBody = objectMapper.writeValueAsString(CREATE_STUDENT_REQUEST_BODY_MAP)
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        //...
    }

    // ...
    @Test
    fun testUnauthorizedRequest() {
        val patientPostBody = objectMapper.writeValueAsString(CREATE_STUDENT_REQUEST_BODY_MAP)
        every { sekiClient.validate(any()) } throws SekiClientException("FOO")
        //...
    }
}
```

This will no longer work, because the new auth chain needs the token to have valid JWT segments, and because we now don't want to mock Seki, per se, but a higher-level auth construct.

The module [product-spring-jwt-auth-mocks](product-spring-jwt-auth-mocks) contains some mocks to help you with this.  For the above scenario, you might do something like this:

First add 

```kotlin
testImplementation(libs.product.spring.jwt.auth.mocks)
```

to your service's build.gradle.kts

Then modify like this:

```kotlin
@WebMvcTest(ControllerUnderTest::class)
@Import(SharedConfigurationReference::class, JwtAuthMockConfig::class) // include mock config object
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ControllerUnderTestIntegrationTest(
    @Autowired val mockMvc: MockMvc
) {
    companion object {
        // ...

        private const val DEFAULT_AUTH_VALUE = "Bearer ${JwtAuthMockHelper.defaultToken}" // use a "real" token

        // you no longer need DEFAULT_AUTH_RESPONSE
    }

    @MockkBean
    lateinit var someMockService: SomeMockService

    // you no longer need SekiClient
    @BeforeEach
    fun setup() {
        clearAllMocks() // must ensure mocks in clean state at beginning of each test.
        JwtAuthMockHelper.reset()
    }

    // ...
    @Test
    fun testValidRequest() {
        val patientPostBody = objectMapper.writeValueAsString(CREATE_STUDENT_REQUEST_BODY_MAP)
        // you no longer need to mock for successful responses; the default managed by JwtAuthMockHelper.reset() is success
        //...
    }

    // ...
    @Test
    fun testUnauthorizedRequest() {
        val patientPostBody = objectMapper.writeValueAsString(CREATE_STUDENT_REQUEST_BODY_MAP)
        
        // here we configure the specific token we want to return.  Specifically in this case it's just an unauthenticated one,
        // but we can configure it to throw exceptions, or return any instance of RoninAuthentication we want, with whatever claims we want.
        // example:
        withMockAuthToken {
            withScopes("admin:write")
            withUserType(RoninUserType.Service)
            withTokenCustomizer {
                isAuthenticated = false
            }
            
            // write your tests / verifications here
        }
        //...
    }
}
```

Contract tests can still use SimpleSekiMock, or can use AuthWireMockHelper in [product-spring-jwt-auth-testutils](../product-spring-jwt-auth-testutils)
if desired to mock JWT Oauth2 authentication.  But again, something like a real token must be provided, as "FOO" is unlikely to work.

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
