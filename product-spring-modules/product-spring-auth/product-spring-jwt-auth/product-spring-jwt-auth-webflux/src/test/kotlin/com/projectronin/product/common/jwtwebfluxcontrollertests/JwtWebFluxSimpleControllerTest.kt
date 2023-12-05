@file:Suppress("ktlint:no-wildcard-imports")

package com.projectronin.product.common.jwtwebfluxcontrollertests

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.projectronin.auth.token.RoninAuthenticationScheme
import com.projectronin.auth.token.RoninAuthenticationSchemeType
import com.projectronin.auth.token.RoninClaims
import com.projectronin.auth.token.RoninLoginProfile
import com.projectronin.auth.token.RoninName
import com.projectronin.auth.token.RoninUser
import com.projectronin.auth.token.RoninUserIdentity
import com.projectronin.auth.token.RoninUserIdentityType
import com.projectronin.auth.token.RoninUserType
import com.projectronin.product.common.auth.COOKIE_STATE_HEADER
import com.projectronin.product.common.auth.COOKIE_STATE_NAME_PREFIX
import com.projectronin.product.common.auth.COOKIE_STATE_QUERY_PARAMETER
import com.projectronin.product.common.auth.RoninJwtAuthenticationToken
import com.projectronin.product.common.auth.SekiJwtAuthenticationToken
import com.projectronin.product.common.auth.sekiRoninEmployeeStrategy
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.testconfigs.BasicPropertiesConfig
import com.projectronin.product.common.testutils.AuthKeyGenerator
import com.projectronin.product.common.testutils.AuthWireMockHelper
import com.projectronin.product.common.testutils.roninClaim
import com.projectronin.product.contracttest.wiremocks.SekiResponseBuilder
import com.projectronin.product.contracttest.wiremocks.SimpleSekiMock
import io.mockk.clearAllMocks
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

@WebFluxTest(JwtWebFluxSimpleController::class)
@Import(JwtWebFluxSharedConfigurationReference::class, BasicPropertiesConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtWebFluxSimpleControllerTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val authHolderBean: AuthHolderBean
) {

    companion object {

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            AuthWireMockHelper.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            AuthWireMockHelper.stop()
        }
    }

    @BeforeEach
    fun setup() {
        clearAllMocks()
        resetToDefault()
        authHolderBean.reset()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        resetToDefault()
        authHolderBean.reset()
    }

    @Test
    fun `should fail with no token`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)
        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith(::verifyUnauthorizedBody)
    }

    @Test
    fun `should fail with wrong issuer`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://example.com")

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith(::verifyUnauthorizedBody)
    }

    @Test
    fun `should fail with expired token`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}") {
            it
                .expirationTime(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                .issueTime(Date.from(Instant.now().minus(61, ChronoUnit.MINUTES)))
        }

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith(::verifyUnauthorizedBody)
    }

    @Test
    fun `should fail with invalid token`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthKeyGenerator.generateRandomRsa(), "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith { response ->
                verifyUnauthorizedBody(response)
                assertThat(authHolderBean.latestRoninAuth).isNull()
            }
    }

    @Test
    fun `should be successful with valid token`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { _ ->
                assertThat(authHolderBean.latestRoninAuth).isNotNull
                val authValue = authHolderBean.latestRoninAuth!!
                assertThat(authValue).isInstanceOfAny(RoninJwtAuthenticationToken::class.java)
            }
    }

    @Test
    fun `should be successful with valid token in cookies`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(COOKIE_STATE_HEADER, "209854")
            .cookie("${COOKIE_STATE_NAME_PREFIX}209854", token)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { _ ->
                assertThat(authHolderBean.latestRoninAuth).isNotNull
                val authValue = authHolderBean.latestRoninAuth!!
                assertThat(authValue).isInstanceOfAny(RoninJwtAuthenticationToken::class.java)
            }
    }

    @Test
    fun `should be successful with valid token in cookies with query parameter`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        webTestClient
            .get()
            .uri("/api/test/sample-object?$COOKIE_STATE_QUERY_PARAMETER=209854")
            .accept(MediaType.APPLICATION_JSON)
            .cookie("${COOKIE_STATE_NAME_PREFIX}209854", token)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { _ ->
                assertThat(authHolderBean.latestRoninAuth).isNotNull
                val authValue = authHolderBean.latestRoninAuth!!
                assertThat(authValue).isInstanceOfAny(RoninJwtAuthenticationToken::class.java)
            }
    }

    @Test
    fun `should fail with state header but no cookie`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(COOKIE_STATE_HEADER, "209854")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith { response ->
                verifyUnauthorizedBody(response)
                assertThat(authHolderBean.latestRoninAuth).isNull()
            }
    }

    @Test
    fun `should fail with state parameter but no cookie`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        webTestClient
            .get()
            .uri("/api/test/sample-object?$COOKIE_STATE_QUERY_PARAMETER=209854")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith { response ->
                verifyUnauthorizedBody(response)
                assertThat(authHolderBean.latestRoninAuth).isNull()
            }
    }

    @Test
    fun `should fail with state header but wrong cookie`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .header(COOKIE_STATE_HEADER, "209854")
            .accept(MediaType.APPLICATION_JSON)
            .cookie("${COOKIE_STATE_NAME_PREFIX}999999", token)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith { response ->
                verifyUnauthorizedBody(response)
                assertThat(authHolderBean.latestRoninAuth).isNull()
            }
    }

    @Test
    fun `should fail with state parameter but wrong cookie`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        webTestClient
            .get()
            .uri("/api/test/sample-object?$COOKIE_STATE_QUERY_PARAMETER=209854")
            .accept(MediaType.APPLICATION_JSON)
            .cookie("${COOKIE_STATE_NAME_PREFIX}999999", token)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith { response ->
                verifyUnauthorizedBody(response)
                assertThat(authHolderBean.latestRoninAuth).isNull()
            }
    }

    @Test
    fun `should fail with cookie but no state`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .cookie("${COOKIE_STATE_NAME_PREFIX}209854", token)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith { response ->
                verifyUnauthorizedBody(response)
                assertThat(authHolderBean.latestRoninAuth).isNull()
            }
    }

    @Test
    fun `should successfully construct ronin claims`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "9bc3abc9-d44d-4355-b81d-57e76218a954",
                userType = RoninUserType.Provider,
                name = RoninName(
                    fullText = "Jennifer Przepiora",
                    familyName = "Przepiora",
                    givenName = listOf("Jennifer"),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Los_Angeles",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingPatientUdpId = "apposnd-231982009",
                    accessingProviderUdpId = "apposnd-eSC7e62xM4tbHbRbARdo0kw3",
                    accessingExternalPatientId = "231982009"
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.ProviderUdpId,
                        tenantId = "apposnd",
                        id = "apposnd-231982009"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.SmartOnFhir,
                        tenantId = "apposnd",
                        id = "231982009"
                    )
                )
            )
        )

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}") { builder ->
            builder.roninClaim(roninClaims)
        }

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { _ ->
                assertThat(authHolderBean.latestRoninAuth).isNotNull
                val authValue = authHolderBean.latestRoninAuth!!
                assertThat(authValue).isInstanceOfAny(RoninJwtAuthenticationToken::class.java)
                assertThat(authValue.roninClaims).isNotNull
                assertThat(authValue.roninClaims).usingRecursiveComparison().isEqualTo(roninClaims)
            }
    }

    @Test
    fun `should be successful with valid token for a second issuer as if we had auth0 running`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val anotherRsaKey = AuthKeyGenerator.generateRandomRsa()
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(anotherRsaKey, issuerPath = "/second-issuer")

        val token = AuthWireMockHelper.generateToken(anotherRsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}/second-issuer")

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { _ ->
                assertThat(authHolderBean.latestRoninAuth).isNotNull
                val authValue = authHolderBean.latestRoninAuth!!
                assertThat(authValue).isInstanceOfAny(RoninJwtAuthenticationToken::class.java)
            }
    }

    @Test
    fun `should be successful with seki token`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId)

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .patientRoninId("apposnd-KIVb2ypkGeX90dOOYxpD4Z")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { _ ->
                assertThat(authHolderBean.latestRoninAuth).isNotNull
                val authValue = authHolderBean.latestRoninAuth!!
                assertThat(authValue).isInstanceOfAny(SekiJwtAuthenticationToken::class.java)
                assertThat(authValue.tenantId).isEqualTo(builder.tenantId)
                assertThat(authValue.userId).isEqualTo(builder.sekiUserId.toString())
                assertThat(authValue.udpId).isEqualTo(builder.udpId)
                assertThat(authValue.userFirstName).isEqualTo(builder.firstName)
                assertThat(authValue.userLastName).isEqualTo(builder.lastName)
                assertThat(authValue.userFullName).isEqualTo(builder.fullName)

                verify(
                    exactly(1),
                    getRequestedFor(urlMatching("/seki/session/validate.*"))
                        .withQueryParam("token", equalTo(token))
                )
            }
    }

    @Test
    fun `should fail auth for token that seki won't validate`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId)

        SimpleSekiMock.unsuccessfulValidate(token)

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith(::verifyUnauthorizedBody)
    }

    @Test
    fun `should fail auth for token that won't validate internally`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(
            secret = "zOKLgXVSxS1sM1XBT4fIsq36ZBV0l9YHqeXhWOfDECeuiXKwm7j88Zpd0NY4sRQQGxrUKJJawzqkSNlBph1odPQXzJkHIA3jyhNb",
            user = userId
        )

        val builder = SekiResponseBuilder(token)
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/sample-object")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .consumeWith { result ->
                verifyUnauthorizedBody(result)

                // here we verify against the secret and expect Seki itself never to be called.
                verify(
                    exactly(0),
                    getRequestedFor(urlMatching("/seki/session/validate.*"))
                        .withQueryParam("token", equalTo(token))
                )
            }
    }

    @Test
    fun `should reject a tenant-id specific object`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, "apposnd")

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .patientRoninId("apposnd-KIVb2ypkGeX90dOOYxpD4Z")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/object/mdaoc/by/tenant")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isForbidden
            .expectBody()
            .consumeWith(::verifyForbiddenBody)
    }

    @Test
    fun `should accept a tenant-id specific object`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, "apposnd")

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .patientRoninId("apposnd-KIVb2ypkGeX90dOOYxpD4Z")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/object/apposnd/by/tenant")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should reject a tenant-id plus patient specific object`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, "apposnd")

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .patientRoninId("apposnd-KIVb2ypkGeX90dOOYxpD4Z")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/object/mdaoc/by/tenant/and/123/patient")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isForbidden
            .expectBody()
            .consumeWith(::verifyForbiddenBody)
    }

    @Test
    fun `should reject a tenant-id plus patient specific object when only patient is wrong`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, "apposnd")

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .patientRoninId("apposnd-KIVb2ypkGeX90dOOYxpD4Z")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/object/apposnd/by/tenant/and/123/patient")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isForbidden
            .expectBody()
            .consumeWith(::verifyForbiddenBody)
    }

    @Test
    fun `should accept a tenant-id plus patient specific object`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, "apposnd")

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .patientRoninId("apposnd-KIVb2ypkGeX90dOOYxpD4Z")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/object/apposnd/by/tenant/and/apposnd-KIVb2ypkGeX90dOOYxpD4Z/patient")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should be ok if the token doesn't have patient though`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, "apposnd")

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/object/apposnd/by/tenant/and/apposnd-KIVb2ypkGeX90dOOYxpD4Z/patient")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    // I'm not going to test Auth0 as an issuer here, since Auth0 functions exactly like any other Oauth2 server here, and we're already testing the
    // jwks and openid config resolution that auth0 uses.  I'm going to use a _example_ token generated for M2M chokuto in DEV to prove that those tokens
    // can be used to secure endpoints, verifying scopes.

    @Test
    fun `should successfully get object requiring specific scope`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}") { builder ->
            builder
                .claim("scope", "session:read phone_user:create phone_user:update thing_requiring_scope:read")
        }

        webTestClient
            .get()
            .uri("/api/test/object-requiring-role")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should successfully get object requiring specific scope when using a collection`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}") { builder ->
            builder.claim(
                "scope",
                listOf(
                    "thing_requiring_scope:read",
                    "thing_requiring_scope:write"
                )
            )
        }

        webTestClient
            .get()
            .uri("/api/test/object-requiring-role")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should fail to get object requiring specific scope`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}") { builder ->
            builder
                .claim("scope", "session:read phone_user:create phone_user:update")
        }

        webTestClient
            .get()
            .uri("/api/test/object-requiring-role")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should fail to get object requiring specific scope when there are no authorities`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}")

        webTestClient
            .get()
            .uri("/api/test/object-requiring-role")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should fail employee check for non-employee`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "9bc3abc9-d44d-4355-b81d-57e76218a954",
                userType = RoninUserType.Provider,
                name = RoninName(
                    fullText = "Jennifer Przepiora",
                    familyName = "Przepiora",
                    givenName = listOf("Jennifer"),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Los_Angeles",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingPatientUdpId = "apposnd-231982009",
                    accessingProviderUdpId = "apposnd-eSC7e62xM4tbHbRbARdo0kw3",
                    accessingExternalPatientId = "231982009"
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.ProviderUdpId,
                        tenantId = "apposnd",
                        id = "apposnd-231982009"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.SmartOnFhir,
                        tenantId = "apposnd",
                        id = "231982009"
                    )
                )
            )
        )

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}") { builder ->
            builder.roninClaim(roninClaims)
        }

        webTestClient
            .get()
            .uri("/api/test/sample-object-for-employees-only")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should succeed employee check for employee`() {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "9bc3abc9-d44d-4355-b81d-57e76218a954",
                userType = RoninUserType.RoninEmployee,
                name = RoninName(
                    fullText = "Jennifer Przepiora",
                    familyName = "Przepiora",
                    givenName = listOf("Jennifer"),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Los_Angeles",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingPatientUdpId = "apposnd-231982009",
                    accessingProviderUdpId = "apposnd-eSC7e62xM4tbHbRbARdo0kw3",
                    accessingExternalPatientId = "231982009"
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.ProviderUdpId,
                        tenantId = "apposnd",
                        id = "apposnd-231982009"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.SmartOnFhir,
                        tenantId = "apposnd",
                        id = "231982009"
                    )
                )
            )
        )

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}") { builder ->
            builder.roninClaim(roninClaims)
        }

        webTestClient
            .get()
            .uri("/api/test/sample-object-for-employees-only")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should fail employee check for employee from Seki`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, "apposnd")

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/sample-object-for-employees-only")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should succeed employee check for employee from Seki`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, "apposnd")

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
            .identities(listOf(sekiRoninEmployeeStrategy to "google-oauth2|Xv0DPhan8Dzst2Suk14fDqMJRawdXr"))
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/sample-object-for-employees-only")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should fail employee check for test user from Seki`() {
        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, "apposnd")

        val builder = SekiResponseBuilder(token).sekiUserId(UUID.randomUUID())
            .sekiEmail("gloria.thom@example.com")
            .firstName("Gloria")
            .lastName("Thom")
            .fullName("Gloria N Thom")
            .preferredTimezone(ZoneId.of("America/Los_Angeles"))
            .providerRoninId("apposnd-Y7is8muodznCehONsedc")
            .tenantId("apposnd")
            .tenantName("app orchard sandbox")
            .udpId("apposnd-Y7is8muodznCehONsedc")
            .metadata(
                mapOf(
                    "foo" to "bar"
                )
            )
            .identities(listOf(sekiRoninEmployeeStrategy to "auth0|Xv0DPhan8Dzst2Suk14fDqMJRawdXr"))
        SimpleSekiMock.successfulValidate(builder)

        webTestClient
            .get()
            .uri("/api/test/sample-object-for-employees-only")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus().isForbidden
    }

    @Nested
    inner class PreAuthTenantTests {
        @ParameterizedTest
        @ValueSource(strings = ["object-requiring-tenant-read", "object-requiring-tenant-write", "object-requiring-tenant-delete"])
        fun `should fail to interact with tenant`(route: String) {
            AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

            val token = AuthWireMockHelper.generateToken(
                AuthWireMockHelper.rsaKey,
                "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}"
            ) { builder ->
                builder
                    .claim("scope", "something:else")
            }

            webTestClient
                .get()
                .uri("/api/test/$route")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()
                .expectStatus().isForbidden
        }

        @ParameterizedTest
        @CsvSource(
            value = [
                "admin:read,object-requiring-tenant-read",
                "tenant:read,object-requiring-tenant-read",
                "admin:read tenant:read,object-requiring-tenant-read",
                "admin:write,object-requiring-tenant-write",
                "tenant:write,object-requiring-tenant-write",
                "admin:write tenant:write,object-requiring-tenant-write",
                "tenant:delete,object-requiring-tenant-delete",
                "admin:delete tenant:delete,object-requiring-tenant-delete"
            ]
        )
        fun `should interact with tenant`(claim: String, route: String) {
            AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

            val token = AuthWireMockHelper.generateToken(
                AuthWireMockHelper.rsaKey,
                "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}"
            ) { builder ->
                builder
                    .claim("scope", claim)
            }

            webTestClient
                .get()
                .uri("/api/test/$route")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .exchange()
                .expectStatus().isOk
        }
    }

    private fun verifyUnauthorizedBody(result: EntityExchangeResult<ByteArray>) {
        val body: ErrorResponse = objectMapper.readValue(result.responseBody!!)

        assertThat(body.httpStatus).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(body.timestamp).isNotNull
        assertThat(body.status).isEqualTo(401)
        assertThat(body.message).isEqualTo("Authentication Error")
    }

    private fun verifyForbiddenBody(result: EntityExchangeResult<ByteArray>) {
        val body: ErrorResponse = objectMapper.readValue(result.responseBody!!)

        assertThat(body.httpStatus).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(body.timestamp).isNotNull
        assertThat(body.status).isEqualTo(403)
        assertThat(body.message).isEqualTo("Forbidden")
    }
}
