@file:Suppress("ktlint:no-wildcard-imports")

package com.projectronin.product.common.auth.m2m

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.resetToDefault
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import com.projectronin.product.common.auth.token.RoninLoginProfile
import com.projectronin.product.common.config.JsonProvider
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class M2MClientServiceTest {

    companion object {
        private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun setup() {
            wireMockServer.start()
            configureFor(wireMockServer.port())
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            wireMockServer.stop()
        }
    }

    private fun service(
        clock: () -> Clock = { Clock.systemUTC() },
        authPath: String = "oauth"
    ) = M2MClientService(
        httpClient = OkHttpClient.Builder().build(),
        objectMapper = JsonProvider.objectMapper,
        providerUrl = "http://localhost:${wireMockServer.port()}",
        clientId = "07b64afd-ab64-4798-8d87-0ae657bc67ed",
        clientSecret = "KmnDnwj636Dx31bO6dovegSTMgziw4gcwjTDb4C6I3qT4QktHNMC41GIhpDqigov8fnJajLiIOIXAX4Nv2jRkoP1rrlA4fAvyb7G",
        clock = clock,
        authPath = authPath
    )

    @BeforeEach
    fun clearMocks() {
        resetToDefault()
    }

    @Test
    fun `should get a token from the endpoint`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .withRequestBody(containing("baz"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        stubFor(
            post(urlPathMatching("/oauth/token"))
                .withRequestBody(containing("qux"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "BAR",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val service = service()
        assertThat(service.getToken("baz")).isEqualTo("FOO")
        assertThat(service.getToken("qux")).isEqualTo("BAR")
    }

    @Test
    fun `should get a token from the endpoint with a different URL`() {
        stubFor(
            post(urlPathMatching("/auth/token"))
                .withRequestBody(containing("baz"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        stubFor(
            post(urlPathMatching("/auth/token"))
                .withRequestBody(containing("qux"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "BAR",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val service = service(authPath = "auth")
        assertThat(service.getToken("baz")).isEqualTo("FOO")
        assertThat(service.getToken("qux")).isEqualTo("BAR")
    }

    @Test
    fun `should pass on the scopes`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val service = service()
        assertThat(service.getToken("baz", scopes = listOf("super_admin"))).isEqualTo("FOO")

        verify(
            exactly(1),
            postRequestedFor(urlPathMatching("/oauth/token"))
                .withRequestBody(matchingJsonPath("\$.scope", matching("super_admin")))
        )
    }

    @Test
    fun `should get a token for a tenant`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val service = service()
        assertThat(
            /* actual = */ service.getToken(
                audience = "baz",
                requestedProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingProviderUdpId = null,
                    accessingPatientUdpId = null
                )
            )
        ).isEqualTo("FOO")

        verify(
            exactly(1),
            postRequestedFor(urlPathMatching("/oauth/token"))
                .withRequestBody(matchingJsonPath("\$.scope", matching("impersonate_tenant:apposnd")))
        )
    }

    @Test
    fun `should get a token for a tenant without adding additional scopes`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val service = service()
        assertThat(
            /* actual = */ service.getToken(
                audience = "baz",
                scopes = listOf("impersonate_tenant:apposnd"),
                requestedProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingProviderUdpId = null,
                    accessingPatientUdpId = null
                )
            )
        ).isEqualTo("FOO")

        verify(
            exactly(1),
            postRequestedFor(urlPathMatching("/oauth/token"))
                .withRequestBody(matchingJsonPath("\$.scope", matching("impersonate_tenant:apposnd")))
        )
    }

    @Test
    fun `should get a token for a tenant and provider`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val service = service()
        assertThat(
            service.getToken(
                audience = "baz",
                requestedProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingProviderUdpId = "jim",
                    accessingPatientUdpId = null
                )
            )
        ).isEqualTo("FOO")

        verify(
            exactly(1),
            postRequestedFor(urlPathMatching("/oauth/token"))
                .withRequestBody(matchingJsonPath("\$.scope", matching("impersonate_tenant:apposnd impersonate_provider:jim")))
        )
    }

    @Test
    fun `should get a token for a tenant and provider with scope pre-specified`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val service = service()
        assertThat(
            service.getToken(
                audience = "baz",
                scopes = listOf("impersonate_provider:any"),
                requestedProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingProviderUdpId = "jim",
                    accessingPatientUdpId = null
                )
            )
        ).isEqualTo("FOO")

        verify(
            exactly(1),
            postRequestedFor(urlPathMatching("/oauth/token"))
                .withRequestBody(matchingJsonPath("\$.scope", matching("impersonate_provider:any impersonate_tenant:apposnd")))
        )
    }

    @Test
    fun `should get a token for a tenant and patient`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val service = service()
        assertThat(
            service.getToken(
                audience = "baz",
                scopes = listOf("super_admin"),
                requestedProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingProviderUdpId = null,
                    accessingPatientUdpId = "kim"
                )
            )
        ).isEqualTo("FOO")

        verify(
            exactly(1),
            postRequestedFor(urlPathMatching("/oauth/token"))
                .withRequestBody(matchingJsonPath("\$.scope", matching("super_admin impersonate_tenant:apposnd impersonate_patient:kim")))
        )
    }

    @Test
    fun `should get a token for a tenant and provider and patient`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val service = service()
        assertThat(
            service.getToken(
                audience = "baz",
                scopes = listOf("super_admin", "impersonate_provider:jim"),
                requestedProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingProviderUdpId = "jim",
                    accessingPatientUdpId = "kim"
                )
            )
        ).isEqualTo("FOO")

        verify(
            exactly(1),
            postRequestedFor(urlPathMatching("/oauth/token"))
                .withRequestBody(matchingJsonPath("\$.scope", matching("super_admin impersonate_provider:jim impersonate_tenant:apposnd impersonate_patient:kim")))
        )
    }

    @Test
    fun `should get a the same token without re-request but will request again after expiration`() {
        stubFor(
            post(urlPathMatching("/oauth/token")).inScenario("s")
                .whenScenarioStateIs(STARTED)
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "FOO",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
                .willSetStateTo("after request 1")
        )
        stubFor(
            post(urlPathMatching("/oauth/token")).inScenario("s")
                .whenScenarioStateIs("after request 1")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                "access_token": "BAZ",
                                "scope": "bar",
                                "expires_in": 86400,
                                "token_type": "Bearer"
                            }
                            """.trimIndent()
                        )
                )
        )

        val startInstant = Instant.now()
        var clock = Clock.fixed(startInstant, ZoneOffset.UTC)
        val service = service(clock = { clock })
        assertThat(service.getToken("baz")).isEqualTo("FOO")

        assertThat(service.getToken("baz")).isEqualTo("FOO")

        verify(exactly(1), postRequestedFor(urlPathMatching("/oauth/token")))

        clock = Clock.offset(clock, Duration.ofSeconds(86401))

        assertThat(service.getToken("baz")).isEqualTo("BAZ")

        verify(exactly(2), postRequestedFor(urlPathMatching("/oauth/token")))
    }

    @Test
    fun `should throw an exception when it fails to get a token`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(403)
                        .withBody(
                            """{
                                "error": "access_denied",
                                "error_description": "Client has not been granted scopes: impersonate_tenant:ronin"
                            }
                            """.trimIndent()
                        )
                )
        )

        val logWatcher = ListAppender<ILoggingEvent>()
        logWatcher.start()

        val logger = LoggerFactory.getLogger(M2MClientService::class.java) as Logger
        logger.addAppender(logWatcher)

        try {
            val service = service()
            assertThatThrownBy { service.getToken("baz") }
                .isInstanceOf(M2MTokenException::class.java)

            assertThat(logWatcher.list.find { ile -> ile.formattedMessage.contains("Client has not been granted scopes: impersonate_tenant:ronin") }).isNotNull
        } finally {
            logger.detachAppender(logWatcher)
            logWatcher.stop()
        }
    }

    @Test
    fun `should throw an exception fine when no body comes back`() {
        stubFor(
            post(urlPathMatching("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(403)
                )
        )

        val service = service()
        assertThatThrownBy { service.getToken("baz") }
            .isInstanceOf(M2MTokenException::class.java)
    }

    @Test
    fun `should fail if no tenant was specified`() {
        val service = service()
        assertThatThrownBy {
            service.getToken(
                audience = "baz",
                requestedProfile = RoninLoginProfile(
                    accessingTenantId = null,
                    accessingProviderUdpId = null,
                    accessingPatientUdpId = "kim"
                )
            )
        }.isInstanceOf(M2MImpersonationException::class.java)
    }
}
