package com.projectronin.product.contracttest.wiremocks

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import wiremock.com.fasterxml.jackson.databind.JsonNode
import wiremock.com.fasterxml.jackson.databind.ObjectMapper
import wiremock.org.apache.commons.lang3.RandomStringUtils
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * A simple wiremock helper for basic Seki-related operations.
 *
 * Probably the best place to put these mocks would be co-located with the kotlin project that hosts
 * the service, as a separate module that can be consumed as a test dependency.  Seki doesn't have such a place
 * right now, so here it is
 */
object SimpleSekiMock {

    /**
     * Stub out a successful validate response
     */
    fun successfulValidate(builder: SekiResponseBuilder) {
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

    /**
     * Stub out a failed validate response.
     */
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

private fun String?.nullOrQuoted(): String = this?.let { """"$it"""" } ?: "null"

/**
 * Build a response for a specific token.  Currently, provides everything but the token itself by default.  But you can override
 * almost anything.
 */
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
    var metadata: Map<String, Any?> = mapOf("some" to "mock metadata")
    var identities: List<Pair<String, String?>>? = listOf(
        "Elixir.Seki.AuthStrategies.MDAToken" to "SOME_EXTERNAL_USER_ID_FOR_ronin"
    )

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
    fun metadata(metadata: Map<String, Any?>) = apply { this.metadata = metadata }
    fun identities(identities: List<Pair<String, String?>>?) = apply { this.identities = identities }

    fun build(): String = run {
        val metadataString = ObjectMapper()
            .also {
                it.findAndRegisterModules()
            }.writeValueAsString(metadata)
        """
        {
            "user": {
                "email": "$sekiEmail",
                "id": "$sekiUserId",
                ${
        identities?.joinToString(",", prefix = """"identities": [""", postfix = "],") { pair ->
            """
                                            {
                                                "auth_strategy": "${pair.first}"${pair.second?.let { """, "external_user_id": "$it"""" }.orEmpty()}                                                
                                            }
            """.trimIndent()
        } ?: ""
        }
                "name": {
                    "first_name": ${firstName.nullOrQuoted()},
                    "full_name": "$fullName",
                    "last_name": ${lastName.nullOrQuoted()}
                },
                "patient_ronin_id": ${patientRoninId.nullOrQuoted()},
                "preferred_timezone": ${preferredTimezone?.toString().nullOrQuoted()},
                "provider_ronin_id": ${providerRoninId.nullOrQuoted()},
                "tenant_id": "$tenantId",
                "tenant_name": "$tenantName",
                "udp_id": ${udpId.nullOrQuoted()}
            },
            "user_session": {
                "expires_at": "${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusMinutes(10))}",
                "metadata": $metadataString,
                "token_string": "$token"
            }
        }
        """.trimIndent()
    }
}

/**
 * There's a lot in this function.  It exists so a developer, if desired, can call an actual seki validate endpoint and then past the text into a test and have this function spit
 * out a builder that will generate a seki response as much like the real one as possible.  Of course, the developer should take pains to obfuscate the input.  In theory this function
 * does that obfuscation, but you're responsible for checking the output, and removing your original seki response from the codebase.
 */
@Suppress("unused")
fun builderFromString(responseContent: String, obfuscate: Boolean = true): String {
    val tree = ObjectMapper().readTree(responseContent)

    val componentCalls = mutableListOf<String>()

    val userNode = tree["user"]

    val secureRandom = SecureRandom()

    fun obfuscateAndWrapString(inputString: JsonNode): String {
        val originText = inputString.asText()
        val text: String = if (obfuscate) {
            if (originText.matches(".*@.*".toRegex())) {
                "${RandomStringUtils.random(10, 0, 0, true, true, null, secureRandom)}@example.com"
            } else if (originText.matches("http.*".toRegex())) {
                "https://example.com/${RandomStringUtils.random(10, 0, 0, true, true, null, secureRandom)}"
            } else if (originText.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$".toRegex())) {
                UUID.randomUUID().toString()
            } else {
                RandomStringUtils.random(originText.length, 0, 0, true, true, null, secureRandom)
            }
        } else {
            originText
        }
        return """"$text""""
    }

    fun addField(
        parentNode: JsonNode,
        fieldName: String,
        functionName: String,
        nullValue: String = "null",
        handler: (JsonNode) -> String = { inputString ->
            obfuscateAndWrapString(inputString)
        }
    ) {
        val callToAdd: String = if (parentNode.has(fieldName)) {
            val fieldValue = parentNode[fieldName]
            if (fieldValue.isNull) {
                """.$functionName($nullValue)"""
            } else {
                """.$functionName(${handler(fieldValue)})"""
            }
        } else {
            """.$functionName($nullValue)"""
        }
        componentCalls += callToAdd
    }

    addField(userNode, "id", "sekiUserId") { node -> if (obfuscate) "UUID.randomUUID()" else """UUID.fromString("${node.asText()}")""" }
    addField(userNode, "email", "sekiEmail")
    addField(userNode, "patient_ronin_id", "patientRoninId")
    addField(userNode, "preferred_timezone", "preferredTimezone") { node -> """ZoneId.of("${node.asText()}")""" }
    addField(userNode, "provider_ronin_id", "providerRoninId")
    addField(userNode, "tenant_id", "tenantId")
    addField(userNode, "tenant_name", "tenantName")
    addField(userNode, "udp_id", "udpId")

    addField(userNode, "identities", "identities", nullValue = "[]") { identitiesNode ->
        """
            listOf(${
        identitiesNode.map { identityNode ->
            """"${identityNode["auth_strategy"].asText()}" to ${obfuscateAndWrapString(identityNode["external_user_id"])}"""
        }
            .joinToString(",")
        })
        """.trimIndent()
    }

    val nameNode = userNode["name"]

    addField(nameNode, "first_name", "firstName")
    addField(nameNode, "last_name", "lastName")
    addField(nameNode, "full_name", "fullName")

    val userSessionNode = tree["user_session"]
    addField(userSessionNode, "metadata", "metadata", nullValue = "{}") { metadataNode ->

        fun handle(node: JsonNode): String {
            return if (node.isTextual) {
                obfuscateAndWrapString(node)
            } else if (node.isArray) {
                """listOf(${node.elements().asSequence().map { handle(it) }.joinToString(",\n")}])"""
            } else if (node.isObject) {
                """mapOf(${node.fields().asSequence().map { """"${it.key}" to ${handle(it.value)}""" }.joinToString(",\n")})"""
            } else {
                node.asText()
            }
        }

        handle(metadataNode)
    }

    return """
        SekiResponseBuilder(token)
            ${componentCalls.joinToString("\n            ")}
    """.trimIndent()
}
