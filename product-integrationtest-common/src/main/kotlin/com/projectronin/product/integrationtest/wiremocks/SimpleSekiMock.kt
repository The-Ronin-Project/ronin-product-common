package com.projectronin.product.integrationtest.wiremocks

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

// Probably the best place to put these mocks would be co-located with the kotlin project that hosts
// the service, as a separate module that can be consumed as a test dependency.  Seki doesn't have such a place
// right now, so here it is.

private fun nullOrQuotedString(nullableString: String?): String = nullableString?.let { """"$it"""" } ?: "null"

class SekiResponseBuilder(val token: String) {
    var sekiUserId: UUID = UUID.randomUUID()
    var sekiEmail: String = "${sekiUserId.toString().substring(0, 8)}_manually_created_user@projectronin.com"
    var firstName: String? = null
    var lastName: String? = null
    var fullName: String = ""
    var patientRoninId: String? = null
    var preferredTimezone: ZoneId? = ZoneId.of("America/Los_Angeles")
    var providerRoninId: String? = "ejh3j95h-759944"
    var tenantId: String = "ejh3j95h"
    var tenantName: String = "cerncode"
    var udpId: String? = providerRoninId
    var metadata: Map<String, Any> = mapOf("some" to "mock metadata")

    fun sekiUserId(sekiUserId: UUID) = apply { this.sekiUserId = sekiUserId }
    fun sekiEmail(sekiEmail: String) = apply { this.sekiEmail = sekiEmail }
    fun firstName(firstName: String?) = apply { this.firstName = firstName }
    fun lastName(lastName: String?) = apply { this.lastName = lastName }
    fun fullName(fullName: String) = apply { this.fullName = fullName }
    fun patientRoninId(patientRoninId: String?) = apply { this.patientRoninId = patientRoninId }
    fun preferredTimezone(preferredTimezone: ZoneId?) = apply { this.preferredTimezone = preferredTimezone }
    fun providerRoninId(providerRoninId: String?) = apply { this.providerRoninId = providerRoninId }
    fun tenantId(tenantId: String) = apply { this.tenantId = tenantId }
    fun tenantName(tenantName: String) = apply { this.tenantName = tenantName }
    fun udpId(udpId: String?) = apply { this.udpId = udpId }
    fun metadata(metadata: Map<String, Any>) = apply { this.metadata = metadata }

    fun build(): String = """
                            {
                                "user": {
                                    "email": "$sekiEmail",
                                    "id": "$sekiUserId",
                                    "identities": [
                                        {
                                            "auth_strategy": "Elixir.Seki.AuthStrategies.MDAToken",
                                            "external_user_id": "SOME_EXTERNAL_USER_ID_FOR_ronin"
                                        }
                                    ],
                                    "name": {
                                        "first_name": ${nullOrQuotedString(firstName)},
                                        "full_name": "$fullName",
                                        "last_name": ${nullOrQuotedString(lastName)}
                                    },
                                    "patient_ronin_id": ${nullOrQuotedString(patientRoninId)},
                                    "preferred_timezone": ${nullOrQuotedString(preferredTimezone?.toString())},
                                    "provider_ronin_id": ${nullOrQuotedString(providerRoninId)},
                                    "tenant_id": "$tenantId",
                                    "tenant_name": "$tenantName",
                                    "udp_id": ${nullOrQuotedString(udpId)}
                                },
                                "user_session": {
                                    "expires_at": "${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusMinutes(10))}",
                                    "metadata": ${ObjectMapper().also {
        it.findAndRegisterModules()
    }.writeValueAsString(metadata)},
                                    "token_string": "$token"
                                }
                            }
    """.trimIndent()
}

object SimpleSekiMock {

    fun successfulValidate(
        builder: SekiResponseBuilder,
    ) {
        stubFor(
            get(urlMatching("/seki/session/validate.*"))
                .withQueryParam("token", equalTo(builder.token))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(builder.build())
                )
        )
    }

    fun unsuccessfulValidate(token: String, httpResponseCode: Int = 401) {
        stubFor(
            get(urlMatching("/seki/session/validate.*"))
                .withQueryParam("token", equalTo(token))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpResponseCode)
                        .withBody(
                            """
                            {
                                "error": "Unauthorized"
                            }
                            """.trimIndent()
                        )
                )
        )
    }
}