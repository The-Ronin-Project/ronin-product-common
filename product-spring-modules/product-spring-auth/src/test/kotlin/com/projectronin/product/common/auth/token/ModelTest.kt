@file:Suppress("ktlint:no-wildcard-imports")

package com.projectronin.product.common.auth.token

import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.auth.token.*
import com.projectronin.product.common.config.JsonProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ModelTest {

    // language=json
    val providerBasicJson = """
        {
            "user": {
                "id": "9bc3abc9-d44d-4355-b81d-57e76218a954",
                "userType": "PROVIDER",
                "name": {
                    "fullText": "Jennifer Przepiora",
                    "familyName": "Przepiora",
                    "givenName": [
                        "Jennifer"
                    ],
                    "prefix": [],
                    "suffix": []
                },
                "preferredTimeZone": "America/Los_Angeles",
                "loginProfile": {
                    "accessingTenantId": "apposnd",
                    "accessingProviderUdpId": "apposnd-eSC7e62xM4tbHbRbARdo0kw3",
                    "accessingPatientUdpId": "apposnd-231982009",
                    "accessingExternalPatientId": "231982009"
                },
                "identities": [
                    {
                        "type": "PROVIDER_UDP_ID",
                        "tenantId": "apposnd",
                        "id": "apposnd-231982009"
                    }
                ],
                "authenticationSchemes": [
                    {
                        "type": "SMART_ON_FHIR",
                        "tenantId": "apposnd",
                        "id": "231982009"
                    }
                ]
            }
        }
    """.trimIndent()

    val jsonWithMissingStuff = """
        {
            "user": {
                "id": "9bc3abc9-d44d-4355-b81d-57e76218a954",
                "userType": "PATIENT",
                "loginProfile": {
                    "accessingTenantId": "apposnd"
                },
                "identities": [
                    {
                        "type": "PATIENT_UDP_ID",
                        "tenantId": "apposnd",
                        "id": "apposnd-231982009"
                    }
                ],
                "authenticationSchemes": [
                    {
                        "type": "AUTH0_OTP",
                        "id": "231982009"
                    }
                ]
            }
        }
    """.trimIndent()

    val jsonWithUnknownIdentifier = """
        {
            "user": {
                "id": "9bc3abc9-d44d-4355-b81d-57e76218a954",
                "userType": "SOMETHING_NEW_SOMEONE_INVENTED",
                "identities": [
                        {
                            "type": "FOO",
                            "tenantId": "apposnd",
                            "id": "apposnd-231982009"
                        }
                ],
                "authenticationSchemes": [
                    {
                        "type": "BAR",
                        "id": "231982009"
                    }
                ]
            }
        }
    """.trimIndent()

    @Test
    fun `should serialize and deserialize correctly`() {
        val deserializedProviderData: RoninClaims = JsonProvider.objectMapper.readValue(providerBasicJson)
        assertThat(deserializedProviderData).usingRecursiveComparison().isEqualTo(
            RoninClaims(
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
        )
    }

    @Test
    fun `should serialize and deserialize correctly when stuff is missing`() {
        val deserializedProviderData: RoninClaims = JsonProvider.objectMapper.readValue(jsonWithMissingStuff)
        assertThat(deserializedProviderData).usingRecursiveComparison().isEqualTo(
            RoninClaims(
                user = RoninUser(
                    id = "9bc3abc9-d44d-4355-b81d-57e76218a954",
                    userType = RoninUserType.Patient,
                    name = null,
                    preferredTimeZone = null,
                    loginProfile = RoninLoginProfile(
                        accessingTenantId = "apposnd",
                        accessingPatientUdpId = null,
                        accessingProviderUdpId = null,
                        accessingExternalPatientId = null
                    ),
                    identities = listOf(
                        RoninUserIdentity(
                            type = RoninUserIdentityType.PatientUdpId,
                            tenantId = "apposnd",
                            id = "apposnd-231982009"
                        )
                    ),
                    authenticationSchemes = listOf(
                        RoninAuthenticationScheme(
                            type = RoninAuthenticationSchemeType.Auth0OTP,
                            tenantId = null,
                            id = "231982009"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `should be round-trip serializable`() {
        val claims = RoninClaims(
            user = RoninUser(
                id = "9bc3abc9-d44d-4355-b81d-57e76218a954",
                userType = RoninUserType.Patient,
                name = null,
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "apposnd",
                    accessingPatientUdpId = null,
                    accessingProviderUdpId = null,
                    accessingExternalPatientId = null
                ),
                preferredTimeZone = "America/Los_Angeles",
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.PatientUdpId,
                        tenantId = "apposnd",
                        id = "apposnd-231982009"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.Auth0OTP,
                        tenantId = null,
                        id = "231982009"
                    )
                )
            )
        )
        val serializedForm = JsonProvider.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(claims)
        val deserializedClaims = JsonProvider.objectMapper.readValue(serializedForm, RoninClaims::class.java)

        assertThat(deserializedClaims).usingRecursiveComparison().isEqualTo(claims)
    }

    @Test
    @Suppress("invisible_member")
    fun `should serialize and deserialize correctly when unknown identifiers are used`() {
        val deserializedProviderData: RoninClaims = JsonProvider.objectMapper.readValue(jsonWithUnknownIdentifier)
        assertThat(deserializedProviderData).usingRecursiveComparison().isEqualTo(
            RoninClaims(
                user = RoninUser(
                    id = "9bc3abc9-d44d-4355-b81d-57e76218a954",
                    userType = RoninUserType.Unknown("SOMETHING_NEW_SOMEONE_INVENTED"),
                    name = null,
                    preferredTimeZone = null,
                    loginProfile = null,
                    identities = listOf(
                        RoninUserIdentity(
                            type = RoninUserIdentityType.Unknown("FOO"),
                            tenantId = "apposnd",
                            id = "apposnd-231982009"
                        )
                    ),
                    authenticationSchemes = listOf(
                        RoninAuthenticationScheme(
                            type = RoninAuthenticationSchemeType.Unknown("BAR"),
                            tenantId = null,
                            id = "231982009"
                        )
                    )
                )
            )
        )
    }

    @Test
    @Suppress("invisible_member")
    fun `the various pseudo-enums should work`() {
        fun <T> checkEquality(left: T, right: T) {
            assertThat(left).isEqualTo(right)
            assertThat(left == right).isTrue
        }

        fun <T> checkInequality(left: T, right: T) {
            assertThat(left).isNotEqualTo(right)
            assertThat(left == right).isFalse()
        }

        fun <T> checkSameness(left: T, right: T) {
            assertThat(left).isSameAs(right)
        }

        checkEquality(RoninUserType.Patient, RoninUserType.Patient)
        checkEquality(RoninUserType.Patient, RoninUserType.Unknown(RoninUserType.Patient.value))
        checkEquality(RoninUserType.Unknown("FOO"), RoninUserType.Unknown("FOO"))
        checkInequality(RoninUserType.Patient, RoninUserType.Provider)
        checkInequality(RoninUserType.Patient, RoninUserType.Unknown("FOO"))
        checkSameness(RoninUserType.Patient, RoninUserType.forValue(RoninUserType.Patient.value))

        checkEquality(RoninUserIdentityType.PatientUdpId, RoninUserIdentityType.PatientUdpId)
        checkEquality(RoninUserIdentityType.PatientUdpId, RoninUserIdentityType.Unknown(RoninUserIdentityType.PatientUdpId.value))
        checkEquality(RoninUserIdentityType.Unknown("FOO"), RoninUserIdentityType.Unknown("FOO"))
        checkInequality(RoninUserIdentityType.PatientUdpId, RoninUserIdentityType.ProviderUdpId)
        checkInequality(RoninUserIdentityType.PatientUdpId, RoninUserIdentityType.Unknown("FOO"))
        checkSameness(RoninUserIdentityType.PatientUdpId, RoninUserIdentityType.forValue(RoninUserIdentityType.PatientUdpId.value))

        checkEquality(RoninAuthenticationSchemeType.Auth0OTP, RoninAuthenticationSchemeType.Auth0OTP)
        checkEquality(RoninAuthenticationSchemeType.MDAToken, RoninAuthenticationSchemeType.Unknown(RoninAuthenticationSchemeType.MDAToken.value))
        checkEquality(RoninAuthenticationSchemeType.Unknown("FOO"), RoninAuthenticationSchemeType.Unknown("FOO"))
        checkInequality(RoninAuthenticationSchemeType.MDAToken, RoninAuthenticationSchemeType.Auth0OTP)
        checkInequality(RoninAuthenticationSchemeType.SmartOnFhir, RoninAuthenticationSchemeType.Unknown("FOO"))
        checkSameness(RoninAuthenticationSchemeType.Auth0OTP, RoninAuthenticationSchemeType.forValue(RoninAuthenticationSchemeType.Auth0OTP.value))
    }
}
