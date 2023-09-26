package com.projectronin.product.common.auth

import com.github.tomakehurst.wiremock.client.WireMock
import com.nimbusds.jwt.JWTClaimsSet
import com.projectronin.auth.RoninAuthentication
import com.projectronin.auth.token.RoninAuthenticationScheme
import com.projectronin.auth.token.RoninAuthenticationSchemeType
import com.projectronin.auth.token.RoninClaims
import com.projectronin.auth.token.RoninLoginProfile
import com.projectronin.auth.token.RoninName
import com.projectronin.auth.token.RoninUser
import com.projectronin.auth.token.RoninUserIdentity
import com.projectronin.auth.token.RoninUserIdentityType
import com.projectronin.auth.token.RoninUserType
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.model.Identity
import com.projectronin.product.common.auth.seki.client.model.Name
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.testutils.AuthWireMockHelper
import com.projectronin.product.common.testutils.roninClaim
import com.projectronin.product.contracttest.wiremocks.SekiResponseBuilder
import com.projectronin.product.contracttest.wiremocks.SimpleSekiMock
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.UUID

class RoninClaimsToSekiDataConverterTest {
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
        WireMock.resetToDefault()
    }

    @AfterEach
    fun teardown() {
        WireMock.resetToDefault()
    }

    private val sekiClient = SekiClient(
        "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}/seki/session/validate",
        OkHttpClient.Builder().build(),
        JsonProvider.objectMapper
    )

    @Test
    fun `should be successful with seki auth pair for a seki token`() {
        val authValue = validSekiJwtAuthenticationToken { builder ->
            builder
                .sekiUserId(UUID.randomUUID())
                .sekiEmail("QnrAUvYP6z@example.com")
                .patientRoninId(null)
                .preferredTimezone(ZoneId.of("America/Los_Angeles"))
                .providerRoninId(null)
                .tenantId("I7p8CzcW")
                .tenantName("rqomOiRf")
                .udpId(null)
                .identities(listOf("Elixir.Seki.AuthStrategies.EpicSmartOnFhir" to "zKsYiBLBumNietEkVEDf"))
                .firstName(null)
                .lastName(null)
                .fullName("")
                .metadata(
                    mapOf(
                        "ehr_accessing_external_patient_id" to "6ZgAUx87YmRnDOLrhOS3",
                        "ehr_accessing_ronin_patient_id" to "ntj847z6xSMgmdP6Rysl0dJ3lgCvh",
                        "idp" to "9zq",
                        "token_response" to mapOf(
                            "access_token" to "DRrHb9bGB",
                            "expires_at" to 1679433872,
                            "other_params" to mapOf(
                                "encounter" to "VLd5t1UpsPJiPjsJDgcW",
                                "id_token" to "Vk0eVj05u",
                                "need_patient_banner" to true,
                                "patient" to "ENqB4221LxeryYUel7N3",
                                "scope" to "CZpnvvd2lxkWAkvKyE97kL",
                                "smart_style_url" to "https://example.com/9xKNSlHoSz",
                                "tenant" to "49bd12f2-5c3f-4d1e-9c32-a4f371913b71",
                                "user" to "7bdd08f7-5e67-4de4-90b5-98ddafbbd4a6",
                                "username" to "4L9yw6"
                            ),
                            "refresh_token" to null,
                            "token_type" to "8zWQa8"
                        )
                    )
                )
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isSameAs(authValue.sekiUser)
        assertThat(converter.sekiSession).isSameAs(authValue.sekiSession)
    }

    @Test
    fun `should be successful with seki token for cerner`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "T2s5PXUyPmMxFWrPt1VvDqOU89lXnf",
                userType = RoninUserType.Provider,
                name = RoninName(
                    fullText = "",
                    familyName = "",
                    givenName = listOf(""),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Los_Angeles",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "I7p8CzcW",
                    accessingPatientUdpId = "ntj847z6xSMgmdP6Rysl0dJ3lgCvh",
                    accessingProviderUdpId = null,
                    accessingExternalPatientId = "6ZgAUx87YmRnDOLrhOS3"
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.ProviderFhirId,
                        tenantId = "I7p8CzcW",
                        id = "zKsYiBLBumNietEkVEDf"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.SmartOnFhir,
                        tenantId = "I7p8CzcW",
                        id = "zKsYiBLBumNietEkVEDf"
                    )
                )
            )
        )

        val authValue = validRoninJwtAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isEqualTo(
            User(
                id = "T2s5PXUyPmMxFWrPt1VvDqOU89lXnf",
                identities = listOf(
                    Identity(
                        sekiSmartOnFhirStrategy,
                        "zKsYiBLBumNietEkVEDf"
                    )
                ),
                patientRoninId = "ntj847z6xSMgmdP6Rysl0dJ3lgCvh",
                name = Name(firstName = "", fullName = "", lastName = ""),
                preferredTimezone = "America/Los_Angeles",
                providerRoninId = null,
                tenantId = "I7p8CzcW",
                tenantName = "I7p8CzcW",
                udpId = null,
                email = "idadv1g6TxKhTyHJOTJIZrRw_dummyemail@projectronin.com"
            )
        )
        assertThat(converter.sekiSession).isEqualTo(
            UserSession(
                expiresAt = expiryDateString(authValue),
                metadata = mapOf(
                    "idp" to "ehr",
                    "ehr_accessing_ronin_patient_id" to "ntj847z6xSMgmdP6Rysl0dJ3lgCvh",
                    "ehr_accessing_external_patient_id" to "6ZgAUx87YmRnDOLrhOS3"
                ),
                tokenString = authValue.token.tokenValue
            )
        )
    }

    @Test
    fun `should be successful with seki token for epic`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "bznJRkuqYa9dXHGhjQ2Coh2QaXGur7",
                userType = RoninUserType.Provider,
                name = RoninName(
                    fullText = "v0Ty6bjZvZEONkUU",
                    familyName = "1OQ4Fs4E",
                    givenName = listOf("ZZ3QLss"),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/New_York",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "JIsV6Ec",
                    accessingPatientUdpId = null,
                    accessingProviderUdpId = null,
                    accessingExternalPatientId = "m93yT1hrVibXTA7GCN2o"
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.ProviderFhirId,
                        tenantId = "JIsV6Ec",
                        id = "dhDawD70COltrwg5QiFl"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.SmartOnFhir,
                        tenantId = "JIsV6Ec",
                        id = "dhDawD70COltrwg5QiFl"
                    )
                )
            )
        )

        val authValue = validRoninJwtAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isEqualTo(
            User(
                id = "bznJRkuqYa9dXHGhjQ2Coh2QaXGur7",
                identities = listOf(
                    Identity(
                        sekiSmartOnFhirStrategy,
                        "dhDawD70COltrwg5QiFl"
                    )
                ),
                patientRoninId = null,
                name = Name(firstName = "ZZ3QLss", fullName = "v0Ty6bjZvZEONkUU", lastName = "1OQ4Fs4E"),
                preferredTimezone = "America/New_York",
                providerRoninId = null,
                tenantId = "JIsV6Ec",
                tenantName = "JIsV6Ec",
                udpId = null,
                email = "idDpJ-nmH9vjLyuj5r9ADBVQ_dummyemail@projectronin.com"
            )
        )
        assertThat(converter.sekiSession).isEqualTo(
            UserSession(
                expiresAt = expiryDateString(authValue),
                metadata = mapOf(
                    "idp" to "ehr",
                    "ehr_accessing_external_patient_id" to "m93yT1hrVibXTA7GCN2o",
                    "userfname" to "ZZ3QLss",
                    "userlname" to "1OQ4Fs4E"
                ),
                tokenString = authValue.token.tokenValue
            )
        )
    }

    @Test
    fun `should be successful with seki token for mda`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "nomM3Qc9KMWlNjtwC8WweePBnAI3Nn",
                userType = RoninUserType.Provider,
                name = RoninName(
                    fullText = "gfwe73YC9qTmtqP",
                    familyName = "6rM72L",
                    givenName = listOf("OmgR59Lf"),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Chicago",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "7gQHa",
                    accessingPatientUdpId = "801bec7f-fa59-4ca5-83af-9c088dd5b58b",
                    accessingProviderUdpId = "zorf",
                    accessingExternalPatientId = "CxfNOrfpSjNhsZ1FuPkz"
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.MDAEpicUserID,
                        tenantId = "7gQHa",
                        id = "BYDJYhAadaSkGas1s7vg"
                    ),
                    RoninUserIdentity(
                        type = RoninUserIdentityType.MDAUserProvNPI,
                        tenantId = "7gQHa",
                        id = "4IswBZouY24bR0sEeQ9CvkSdgFplEm"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.MDAToken,
                        tenantId = "7gQHa",
                        id = "EPICUSERID=FOO&UTC=2023:5:23:20:21:13&HASH=BAR"
                    )
                )
            )
        )

        val authValue = validRoninJwtAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isEqualTo(
            User(
                id = "nomM3Qc9KMWlNjtwC8WweePBnAI3Nn",
                identities = listOf(
                    Identity(
                        sekiMdaStrategy,
                        "BYDJYhAadaSkGas1s7vg"
                    ),
                    Identity(
                        RoninUserIdentityType.MDAUserProvNPI.value,
                        "4IswBZouY24bR0sEeQ9CvkSdgFplEm"
                    )
                ),
                patientRoninId = "801bec7f-fa59-4ca5-83af-9c088dd5b58b",
                name = Name(firstName = "OmgR59Lf", fullName = "gfwe73YC9qTmtqP", lastName = "6rM72L"),
                preferredTimezone = "America/Chicago",
                providerRoninId = "zorf",
                tenantId = "7gQHa",
                tenantName = "7gQHa",
                udpId = "zorf",
                email = "idyz2aKeFt4O_Tb6WY4ruQoA_dummyemail@projectronin.com"
            )
        )
        assertThat(converter.sekiSession).isEqualTo(
            UserSession(
                expiresAt = expiryDateString(authValue),
                metadata = mapOf(
                    "idp" to "ehr",
                    "ehr_accessing_ronin_patient_id" to "801bec7f-fa59-4ca5-83af-9c088dd5b58b",
                    "raw_mda_token_string" to "EPICUSERID=FOO&UTC=2023:5:23:20:21:13&HASH=BAR",
                    "ehr_accessing_external_patient_id" to "CxfNOrfpSjNhsZ1FuPkz",
                    "userprovnpi" to "4IswBZouY24bR0sEeQ9CvkSdgFplEm",
                    "userfname" to "OmgR59Lf",
                    "userlname" to "6rM72L",
                    "epicuserid" to "BYDJYhAadaSkGas1s7vg"
                ),
                tokenString = authValue.token.tokenValue
            )
        )
    }

    @Test
    fun `should be successful with seki token for mock`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "ixgpxrTSiIxwaLl4HYNKfB9FaYYXaE",
                userType = RoninUserType.Provider,
                name = RoninName(
                    fullText = "",
                    familyName = "",
                    givenName = listOf(""),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Chicago",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "9jpmw7C",
                    accessingPatientUdpId = null,
                    accessingProviderUdpId = "rryczblB84grBFQg7X2HgoCm",
                    accessingExternalPatientId = null
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.MDAEpicUserID,
                        tenantId = "9jpmw7C",
                        id = "IXhMpwvNFE7YYp43njknDfgnbaaX2"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.MDAToken,
                        tenantId = "9jpmw7C",
                        id = "IXhMpwvNFE7YYp43njknDfgnbaaX2"
                    )
                )
            )
        )

        val authValue = validRoninJwtAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isEqualTo(
            User(
                id = "ixgpxrTSiIxwaLl4HYNKfB9FaYYXaE",
                identities = listOf(
                    Identity(
                        sekiMdaStrategy,
                        "IXhMpwvNFE7YYp43njknDfgnbaaX2"
                    )
                ),
                patientRoninId = null,
                name = Name(firstName = "", fullName = "", lastName = ""),
                preferredTimezone = "America/Chicago",
                providerRoninId = "rryczblB84grBFQg7X2HgoCm",
                tenantId = "9jpmw7C",
                tenantName = "9jpmw7C",
                udpId = "rryczblB84grBFQg7X2HgoCm",
                email = "idQ-3Dl56EXZSXW-KH4KA1SQ_dummyemail@projectronin.com"
            )
        )
        assertThat(converter.sekiSession).isEqualTo(
            UserSession(
                expiresAt = expiryDateString(authValue),
                metadata = mapOf(
                    "idp" to "ehr",
                    "raw_mda_token_string" to "IXhMpwvNFE7YYp43njknDfgnbaaX2",
                    "epicuserid" to "IXhMpwvNFE7YYp43njknDfgnbaaX2"
                ),
                tokenString = authValue.token.tokenValue
            )
        )
    }

    @Test
    fun `should be successful with seki token for patient`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "ZxZd5md6GzuXcavh4iiODrxyrMnUaz",
                userType = RoninUserType.Patient,
                name = RoninName(
                    fullText = "",
                    familyName = "",
                    givenName = listOf(""),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = null,
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "PU0Kdrxs",
                    accessingPatientUdpId = "DAEKE13TZ53tlEN9ltWncu6UbhSVx",
                    accessingProviderUdpId = null,
                    accessingExternalPatientId = null
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.PatientSmsId,
                        tenantId = "PU0Kdrxs",
                        id = "D7Ke4lPLgR"
                    ),
                    RoninUserIdentity(
                        type = RoninUserIdentityType.PatientSmsId,
                        tenantId = "PU0Kdrxs",
                        id = "fd05Xt70ttC1rVeHmH1t9fj08PAr"
                    ),
                    RoninUserIdentity(
                        type = RoninUserIdentityType.PatientSmsId,
                        tenantId = "PU0Kdrxs",
                        id = "IkdH2SNPz5MNuY8rQYGWjQ9LZDcC"
                    ),
                    RoninUserIdentity(
                        type = RoninUserIdentityType.PatientSmsId,
                        tenantId = "PU0Kdrxs",
                        id = "sM15fsJ0qwAsSaej7MzSmFgrp5bq"
                    ),
                    RoninUserIdentity(
                        type = RoninUserIdentityType.PatientSmsId,
                        tenantId = "PU0Kdrxs",
                        id = "MTjZtkrOR0IZJbk2wCV7CkSQTs7C"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.Auth0OTP,
                        tenantId = "PU0Kdrxs",
                        id = "D7Ke4lPLgR"
                    )
                )
            )
        )

        val authValue = validRoninJwtAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isEqualTo(
            User(
                id = "ZxZd5md6GzuXcavh4iiODrxyrMnUaz",
                identities = listOf(
                    Identity(
                        sekiOtpPatientStrategy,
                        "D7Ke4lPLgR"
                    ),
                    Identity(
                        sekiOtpPatientStrategy,
                        "fd05Xt70ttC1rVeHmH1t9fj08PAr"
                    ),
                    Identity(
                        sekiOtpPatientStrategy,
                        "IkdH2SNPz5MNuY8rQYGWjQ9LZDcC"
                    ),
                    Identity(
                        sekiOtpPatientStrategy,
                        "sM15fsJ0qwAsSaej7MzSmFgrp5bq"
                    ),
                    Identity(
                        sekiOtpPatientStrategy,
                        "MTjZtkrOR0IZJbk2wCV7CkSQTs7C"
                    )
                ),
                patientRoninId = "DAEKE13TZ53tlEN9ltWncu6UbhSVx",
                name = Name(firstName = "", fullName = "", lastName = ""),
                preferredTimezone = null,
                providerRoninId = null,
                tenantId = "PU0Kdrxs",
                tenantName = "PU0Kdrxs",
                udpId = "DAEKE13TZ53tlEN9ltWncu6UbhSVx",
                email = "idCFpKPY1Kv5HYVq0VjwfPBw_dummyemail@projectronin.com"
            )
        )
        assertThat(converter.sekiSession).isEqualTo(
            UserSession(
                expiresAt = expiryDateString(authValue),
                metadata = mapOf(
                    "idp" to "AUTH0_OTP",
                    "ehr_accessing_ronin_patient_id" to "DAEKE13TZ53tlEN9ltWncu6UbhSVx"
                ),
                tokenString = authValue.token.tokenValue
            )
        )
    }

    @Test
    fun `should be successful with seki token for ronin`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "SXlirOcxhxku6tz6KS1ZAJvcGEasqJ",
                userType = RoninUserType.RoninEmployee,
                name = RoninName(
                    fullText = "QCdxZfp16S5ASH",
                    familyName = "oaW9Hse",
                    givenName = listOf("JYq05b"),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Chicago",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "G4ag8L7",
                    accessingPatientUdpId = null,
                    accessingProviderUdpId = null,
                    accessingExternalPatientId = null
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.GoogleAccountId,
                        tenantId = "G4ag8L7",
                        id = "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.Auth0GoogleOauth,
                        tenantId = "G4ag8L7",
                        id = "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"
                    )
                )
            )
        )

        val authValue = validRoninJwtAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isEqualTo(
            User(
                id = "SXlirOcxhxku6tz6KS1ZAJvcGEasqJ",
                identities = listOf(
                    Identity(
                        sekiRoninEmployeeStrategy,
                        "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"
                    )
                ),
                patientRoninId = null,
                name = Name(firstName = "JYq05b", fullName = "QCdxZfp16S5ASH", lastName = "oaW9Hse"),
                preferredTimezone = "America/Chicago",
                providerRoninId = null,
                tenantId = "G4ag8L7",
                tenantName = "G4ag8L7",
                udpId = null,
                email = "idQJnbTjdaNCM7Ci3dDVnDeQ_dummyemail@projectronin.com"
            )
        )
        assertThat(converter.sekiSession).isEqualTo(
            UserSession(
                expiresAt = expiryDateString(authValue),
                metadata = mapOf(
                    "idp" to "ronin_employees",
                    "userfname" to "JYq05b",
                    "userlname" to "oaW9Hse"
                ),
                tokenString = authValue.token.tokenValue
            )
        )
    }

    @Test
    fun `should be successful without user`() {
        val roninClaims = RoninClaims(
            user = null
        )

        val authValue = validRoninJwtAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isEqualTo(
            User(
                id = "",
                identities = null,
                patientRoninId = null,
                name = Name(firstName = null, fullName = null, lastName = null),
                preferredTimezone = null,
                providerRoninId = null,
                tenantId = "unknown",
                tenantName = null,
                udpId = null
            )
        )
        assertThat(converter.sekiSession).isEqualTo(
            UserSession(
                expiresAt = expiryDateString(authValue),
                metadata = mapOf(
                    "idp" to "unknown"
                ),
                tokenString = authValue.token.tokenValue
            )
        )
    }

    @Test
    fun `should work with no profile`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "SXlirOcxhxku6tz6KS1ZAJvcGEasqJ",
                userType = RoninUserType.RoninEmployee,
                name = RoninName(
                    fullText = "QCdxZfp16S5ASH",
                    familyName = "oaW9Hse",
                    givenName = listOf("JYq05b"),
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = "America/Chicago",
                loginProfile = null,
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.GoogleAccountId,
                        tenantId = "G4ag8L7",
                        id = "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.Auth0GoogleOauth,
                        tenantId = "G4ag8L7",
                        id = "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"
                    )
                )
            )
        )

        val authValue = validRoninJwtAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isEqualTo(
            User(
                id = "SXlirOcxhxku6tz6KS1ZAJvcGEasqJ",
                identities = listOf(
                    Identity(
                        sekiRoninEmployeeStrategy,
                        "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"
                    )
                ),
                patientRoninId = null,
                name = Name(firstName = "JYq05b", fullName = "QCdxZfp16S5ASH", lastName = "oaW9Hse"),
                preferredTimezone = "America/Chicago",
                providerRoninId = null,
                tenantId = "unknown",
                tenantName = null,
                udpId = null,
                email = "idQJnbTjdaNCM7Ci3dDVnDeQ_dummyemail@projectronin.com"
            )
        )
        assertThat(converter.sekiSession).isEqualTo(
            UserSession(
                expiresAt = expiryDateString(authValue),
                metadata = mapOf(
                    "idp" to "ronin_employees",
                    "userfname" to "JYq05b",
                    "userlname" to "oaW9Hse"
                ),
                tokenString = authValue.token.tokenValue
            )
        )
    }

    @Test
    fun `should work with no name`() {
        val roninClaims = RoninClaims(
            user = RoninUser(
                id = "SXlirOcxhxku6tz6KS1ZAJvcGEasqJ",
                userType = RoninUserType.RoninEmployee,
                name = null,
                preferredTimeZone = "America/Chicago",
                loginProfile = RoninLoginProfile(
                    accessingTenantId = "G4ag8L7",
                    accessingPatientUdpId = null,
                    accessingProviderUdpId = null,
                    accessingExternalPatientId = null
                ),
                identities = listOf(
                    RoninUserIdentity(
                        type = RoninUserIdentityType.GoogleAccountId,
                        tenantId = "G4ag8L7",
                        id = "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"
                    )
                ),
                authenticationSchemes = listOf(
                    RoninAuthenticationScheme(
                        type = RoninAuthenticationSchemeType.Auth0GoogleOauth,
                        tenantId = "G4ag8L7",
                        id = "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"
                    )
                )
            )
        )

        val authValue = validRoninJwtAuthenticationToken { builder ->
            builder.roninClaim(roninClaims)
        }

        val converter = RoninClaimsToSekiDataConverter(authValue)

        assertThat(converter.sekiUser).isEqualTo(
            User(
                id = "SXlirOcxhxku6tz6KS1ZAJvcGEasqJ",
                identities = listOf(
                    Identity(
                        sekiRoninEmployeeStrategy,
                        "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"
                    )
                ),
                patientRoninId = null,
                name = Name(firstName = null, fullName = null, lastName = null),
                preferredTimezone = "America/Chicago",
                providerRoninId = null,
                tenantId = "G4ag8L7",
                tenantName = "G4ag8L7",
                udpId = null,
                email = "idQJnbTjdaNCM7Ci3dDVnDeQ_dummyemail@projectronin.com"
            )
        )
        assertThat(converter.sekiSession).isEqualTo(
            UserSession(
                expiresAt = expiryDateString(authValue),
                metadata = mapOf(
                    "idp" to "ronin_employees"
                ),
                tokenString = authValue.token.tokenValue
            )
        )
    }

    private fun validSekiJwtAuthenticationToken(sekiCustomizer: (SekiResponseBuilder) -> SekiResponseBuilder = { it }): SekiJwtAuthenticationToken {
        val decoder = NimbusJwtDecoder.withSecretKey(AuthWireMockHelper.secretKey(AuthWireMockHelper.sekiSharedSecret)).build()

        val userId = UUID.randomUUID().toString()
        val token = AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId)

        val builder = sekiCustomizer(SekiResponseBuilder(token))
        SimpleSekiMock.successfulValidate(builder)

        return SekiCustomAuthenticationConverter(sekiClient).convert(decoder.decode(token)) as SekiJwtAuthenticationToken
    }

    private fun validRoninJwtAuthenticationToken(claimSetCustomizer: (JWTClaimsSet.Builder) -> JWTClaimsSet.Builder = { it }): RoninJwtAuthenticationToken {
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(AuthWireMockHelper.rsaKey)

        val decoder = NimbusJwtDecoder.withPublicKey(AuthWireMockHelper.rsaKey.toRSAPublicKey()).build()

        val token = AuthWireMockHelper.generateToken(AuthWireMockHelper.rsaKey, "http://127.0.0.1:${AuthWireMockHelper.wireMockPort}") { builder ->
            claimSetCustomizer(
                builder
                    .expirationTime(Date.from(Instant.now().plusSeconds(300)))
            )
        }

        val authToken = RoninCustomAuthenticationConverter().convert(decoder.decode(token))

        assertThat(authToken).isInstanceOf(RoninAuthentication::class.java)
        return authToken as RoninJwtAuthenticationToken
    }

    private fun expiryDateString(authValue: RoninJwtAuthenticationToken) = authValue.token.expiresAt?.toString()?.substring(0, 19)
}
