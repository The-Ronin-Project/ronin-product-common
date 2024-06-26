package com.projectronin.product.common.auth

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.resetToDefault
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
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
import com.projectronin.product.common.config.JsonProvider
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
import java.time.ZoneId
import java.util.UUID

class SekiJwtAuthenticationTokenTest {
    companion object {
        private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun staticSetup() {
            wireMockServer.start()
            configureFor(wireMockServer.port())
        }

        @AfterAll
        @JvmStatic
        fun staticTeardown() {
            wireMockServer.stop()
        }
    }

    @BeforeEach
    fun setup() {
        resetToDefault()
    }

    @AfterEach
    fun teardown() {
        resetToDefault()
    }

    private val sekiClient = SekiClient(
        "http://127.0.0.1:${wireMockServer.port()}/seki/session/validate",
        OkHttpClient.Builder().build(),
        JsonProvider.objectMapper
    )

    @Test
    fun `should get a valid seki token`() {
        val (roninAuthentication, _, rawToken) = validRoninAuthenticationTokenAndRawToken()
        assertThat(roninAuthentication.isAuthenticated).isTrue
        assertThat(roninAuthentication.roninClaims).isNotNull
        assertThat(roninAuthentication.tokenValue).isEqualTo(rawToken)
    }

    @Test
    fun `should be successful with seki token`() {
        val (authValue, builder) = validRoninAuthenticationToken { builder ->
            builder
                .sekiUserId(UUID.randomUUID())
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
        }
        assertThat(authValue).isInstanceOfAny(SekiJwtAuthenticationToken::class.java)
        assertThat(authValue.tenantId).isEqualTo(builder.tenantId)
        assertThat(authValue.userId).isEqualTo(builder.sekiUserId.toString())
        assertThat(authValue.udpId).isEqualTo(builder.udpId)
        assertThat(authValue.userFirstName).isEqualTo(builder.firstName)
        assertThat(authValue.userLastName).isEqualTo(builder.lastName)
        assertThat(authValue.userFullName).isEqualTo(builder.fullName)
    }

    @Test
    fun `should be successful with seki token for cerner`() {
        val (authValue, builder) = validRoninAuthenticationToken { builder ->
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
        assertThat(authValue).isInstanceOfAny(SekiJwtAuthenticationToken::class.java)
        assertThat(authValue.tenantId).isEqualTo(builder.tenantId)
        assertThat(authValue.userId).isEqualTo(builder.sekiUserId.toString())
        assertThat(authValue.udpId).isEqualTo(builder.udpId)
        assertThat(authValue.userFirstName).isEqualTo("")
        assertThat(authValue.userLastName).isEqualTo("")
        assertThat(authValue.userFullName).isEqualTo("")

        assertThat(authValue.roninClaims).usingRecursiveComparison().isEqualTo(
            RoninClaims(
                user = RoninUser(
                    id = builder.sekiUserId.toString(),
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
        )
    }

    @Test
    fun `should be successful with seki token for epic`() {
        val (authValue, builder) = validRoninAuthenticationToken { builder ->
            builder
                .sekiUserId(UUID.randomUUID())
                .sekiEmail("PLAGEaK22p@example.com")
                .patientRoninId(null)
                .preferredTimezone(ZoneId.of("America/New_York"))
                .providerRoninId(null)
                .tenantId("JIsV6Ec")
                .tenantName("8vYcHmgBtuvE43MVCMCWFuJo")
                .udpId(null)
                .identities(listOf("Elixir.Seki.AuthStrategies.EpicSmartOnFhir" to "dhDawD70COltrwg5QiFl"))
                .firstName("ZZ3QLss")
                .lastName("1OQ4Fs4E")
                .fullName("v0Ty6bjZvZEONkUU")
                .metadata(
                    mapOf(
                        "ehr_accessing_external_patient_id" to "m93yT1hrVibXTA7GCN2o",
                        "ehr_accessing_ronin_patient_id" to null,
                        "idp" to "O1L",
                        "token_response" to mapOf(
                            "access_token" to "enrvvZbTh",
                            "expires_at" to 1679433872,
                            "other_params" to mapOf(
                                "__epic.dstu2.patient" to "kCpgWMOw4sKQ9MTDprn8",
                                "clientHostSource" to "https://example.com/F5LQJClmde",
                                "epicUserId" to "U8G6NmADuRl8mGYUdLI84",
                                "fName" to "oeQxxVl",
                                "fhirPatientId" to "dkbiG5oqZG0LfumxUx9y",
                                "id_token" to "H0Tfo1mxB",
                                "lName" to "hWfKrGmU",
                                "loginDepartment" to "nLrHwyULEPQ9m8mzpY7b",
                                "need_patient_banner" to "eRnrY",
                                "patient" to "JzWa0Y9y0oG24pI0JKmi",
                                "scope" to "riRwld4qP6Xi7P81UJ2Ntud1EHt9tkth7nN6BwcPsRverBNer23BKN461UhZ3811irG5n6lSAGP10NmlqfIggFqkAyP1EOOnDyAe0kpeC8o6KX",
                                "smart_style_url" to "https://example.com/Njg5E8Wz2C",
                                "state" to "aSzGK0Up2esQK1x3JOV5xfc7cjYq3OcqP22N4HmUhuUMSDezrYt1KoBO3Hr1zjJQT55eWewfkozK2hPHeY5H56BlWeHsEkNzoLn6",
                                "sysLogin" to "T7KXMgoot8DHyiu9eJFX",
                                "userProvFHIRid" to "IfTLbfUio5cOzJsrkYdQ"
                            ),
                            "refresh_token" to null,
                            "token_type" to "DXSMaz"
                        )
                    )
                )
        }
        assertThat(authValue).isInstanceOfAny(SekiJwtAuthenticationToken::class.java)
        assertThat(authValue.tenantId).isEqualTo(builder.tenantId)
        assertThat(authValue.userId).isEqualTo(builder.sekiUserId.toString())
        assertThat(authValue.udpId).isEqualTo(builder.udpId)
        assertThat(authValue.userFirstName).isEqualTo(builder.firstName)
        assertThat(authValue.userLastName).isEqualTo(builder.lastName)
        assertThat(authValue.userFullName).isEqualTo(builder.fullName)

        assertThat(authValue.roninClaims).usingRecursiveComparison().isEqualTo(
            RoninClaims(
                user = RoninUser(
                    id = builder.sekiUserId.toString(),
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
        )
    }

    @Test
    fun `should be successful with seki token for mda`() {
        val (authValue, builder) = validRoninAuthenticationToken { builder ->
            builder
                .sekiUserId(UUID.randomUUID())
                .sekiEmail("I5W3SHyxxC@example.com")
                .patientRoninId(null)
                .preferredTimezone(ZoneId.of("America/Chicago"))
                .providerRoninId("zorf")
                .tenantId("7gQHa")
                .tenantName("3w6")
                .udpId(null)
                .identities(listOf("Elixir.Seki.AuthStrategies.MDAToken" to "BYDJYhAadaSkGas1s7vg"))
                .firstName("OmgR59Lf")
                .lastName("6rM72L")
                .fullName("gfwe73YC9qTmtqP")
                .metadata(
                    mapOf(
                        "ehr_accessing_external_patient_id" to "CxfNOrfpSjNhsZ1FuPkz",
                        "ehr_accessing_ronin_patient_id" to "801bec7f-fa59-4ca5-83af-9c088dd5b58b",
                        "epicuserid" to "BjqNkvV2KQY5twzcaoTZ",
                        "fhirpatid" to "E3zC5e4Xz0MprW7zUys6",
                        "fname" to "qUayh4ao",
                        "hash" to "3H1vhhjHKf4ag4BCAbwb",
                        "idp" to "Goj",
                        "lname" to "aw3xrC",
                        "maskid" to "eQc1xDPY9KPjcwfJTcsh",
                        "mdauserid" to "4abZs3tzMEO4YcB8FBh4",
                        "mname" to null,
                        "patid" to "d2jd11qLKP6mypqlkWsr",
                        "raw_mda_token_string" to "ws5",
                        "userfname" to "jHhQuNoY",
                        "userlname" to "Tz72QT",
                        "userprovfhirid" to null,
                        "userprovnpi" to null,
                        "utc" to "nluzgQ8Ixakq0MbPhMUL"
                    )
                )
        }
        assertThat(authValue).isInstanceOfAny(SekiJwtAuthenticationToken::class.java)
        assertThat(authValue.tenantId).isEqualTo(builder.tenantId)
        assertThat(authValue.userId).isEqualTo(builder.sekiUserId.toString())
        assertThat(authValue.udpId).isEqualTo(builder.udpId)
        assertThat(authValue.userFirstName).isEqualTo(builder.firstName)
        assertThat(authValue.userLastName).isEqualTo(builder.lastName)
        assertThat(authValue.userFullName).isEqualTo(builder.fullName)

        assertThat(authValue.roninClaims).usingRecursiveComparison().isEqualTo(
            RoninClaims(
                user = RoninUser(
                    id = builder.sekiUserId.toString(),
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
                        )
                    ),
                    authenticationSchemes = listOf(
                        RoninAuthenticationScheme(
                            type = RoninAuthenticationSchemeType.MDAToken,
                            tenantId = "7gQHa",
                            id = "BYDJYhAadaSkGas1s7vg"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `should be successful with seki token for mock`() {
        val (authValue, builder) = validRoninAuthenticationToken { builder ->
            builder
                .sekiUserId(UUID.randomUUID())
                .sekiEmail("qFe2EHsTUx@example.com")
                .patientRoninId(null)
                .preferredTimezone(ZoneId.of("America/Chicago"))
                .providerRoninId("rryczblB84grBFQg7X2HgoCm")
                .tenantId("9jpmw7C")
                .tenantName("kd5WjyaUDw9lMTB7y8JUhMsh")
                .udpId(null)
                .identities(listOf("Elixir.Seki.AuthStrategies.MDAToken" to "IXhMpwvNFE7YYp43njknDfgnbaaX2"))
                .firstName(null)
                .lastName(null)
                .fullName("")
                .metadata(mapOf("some" to "BVYVfLH0OVy3m"))
        }
        assertThat(authValue).isInstanceOfAny(SekiJwtAuthenticationToken::class.java)
        assertThat(authValue.tenantId).isEqualTo(builder.tenantId)
        assertThat(authValue.userId).isEqualTo(builder.sekiUserId.toString())
        assertThat(authValue.udpId).isEqualTo(builder.udpId)
        assertThat(authValue.userFirstName).isEqualTo("")
        assertThat(authValue.userLastName).isEqualTo("")
        assertThat(authValue.userFullName).isEqualTo(builder.fullName)

        assertThat(authValue.roninClaims).usingRecursiveComparison().isEqualTo(
            RoninClaims(
                user = RoninUser(
                    id = builder.sekiUserId.toString(),
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
        )
    }

    @Test
    fun `should be successful with seki token for patient`() {
        val (authValue, builder) = validRoninAuthenticationToken { builder ->
            builder
                .sekiUserId(UUID.randomUUID())
                .sekiEmail("Jwn0Ps7vSj@example.com")
                .patientRoninId(null)
                .preferredTimezone(null)
                .providerRoninId(null)
                .tenantId("PU0Kdrxs")
                .tenantName("JcdcVVu")
                .udpId("DAEKE13TZ53tlEN9ltWncu6UbhSVx")
                .identities(
                    listOf(
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "D7Ke4lPLgR",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "fd05Xt70ttC1rVeHmH1t9fj08PAr",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "IkdH2SNPz5MNuY8rQYGWjQ9LZDcC",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "sM15fsJ0qwAsSaej7MzSmFgrp5bq",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "MTjZtkrOR0IZJbk2wCV7CkSQTs7C",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "9BpKdMga85J6zIUqXrdiM68KQuaA",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "rLyi3GRpaSdepDawPf7LiY5Gq2uO",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "IsJTev8AmvjjcVpgKs4e2OHN8O3e",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "OyFpWd5RrWX8Me4SkQpjv8hLmBso",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "YUUc2nmKKE6nQxeNeqlMsFIdg6zq",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "thAUnOQbesA4LP0CrpXn8Pno8nEA",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "tjlcMDFAqWUn6j0wpQTLIRv9edp4",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "hHKHMYDqddl7JhzucZoRELRrcj9s",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "uel7J6IX8wigIqUwsltHBTCJLBPG",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "fZDDZTUNnuPfa49wzcnjtpewi6WB",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "cQRkt9KIoU6g9HUSjD33yi2q7xVP",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "5a4Dny1Wl4k8bAnp4q8S2sc34cAu",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "e6cfuQij03IRKnraj4puSAeUdgz6",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "bxICsezBX1zQydp4qSidNgedIzFO",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "7gY4kJDm7HK1b8cYzZOOnjEbGitr",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "jvWuoFmfQeKw52yK9FhrOpvNataH",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "oY7cbXDnp4W46ls9Vel9BHepCtrF",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "wbvvsXzVSQWIjMbsfH0V9n21JXtt",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "EbruBxgmOo1Lc2oSB0IB5E9VWS16",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "e0hROLw1i5jSKjOG89kb5oHghXlm",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "eZpsPhtYCpBK7C1qA0sUJiqGxu8J",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "T1JKzTjDomriBSrRGfY3oSrcP8wD",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "WKoLUo9WRkaYSMyEJYX2OohtsREk",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "awyWxEwEgk8dGNKneW14rqltREPo",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "qGtb20LiMp9mjz9ieL4FoYWhLwbj",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "5wAvP8UAE2mkiR1IeQ9WybTUkeeL",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "ZI8Et9JEX9mpYoDNS2xp4qOImJFU",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "axkfh45d50DVbLxN5v87fxGcwI7F",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "QhUaA2NynhfPfak09sh0vD3zWznC",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "yYkczXnKUwmRPPsJQdQVPs9TjNh6",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "sMCKR7Tcd6svL2GTEvIdSsNYdaeM",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "CUj6lym1ffZiTUhnirEz9eF6HZBO",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "9O6Xs9C62YyaND9jrHhCtxcT7YYM",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "PYJP0A74qXvlXuxPGtBNXoCy9NrN",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "pQmkju4AJVFUh74KTISQrBJ3gA5i",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "qRBHNRS6jRjV7uuN5DB8xxzfCsRH",
                        "Elixir.Seki.AuthStrategies.OtpPatientApp" to "aTtoF2vWVPgbYJ0jQgdvpLgSqQli"
                    )
                )
                .firstName("")
                .lastName("")
                .fullName("")
                .metadata(mapOf("idp" to "juHiH"))
        }
        assertThat(authValue).isInstanceOfAny(SekiJwtAuthenticationToken::class.java)
        assertThat(authValue.tenantId).isEqualTo(builder.tenantId)
        assertThat(authValue.userId).isEqualTo(builder.sekiUserId.toString())
        assertThat(authValue.udpId).isEqualTo(builder.udpId)
        assertThat(authValue.userFirstName).isEqualTo(builder.firstName)
        assertThat(authValue.userLastName).isEqualTo(builder.lastName)
        assertThat(authValue.userFullName).isEqualTo(builder.fullName)

        assertThat(authValue.roninClaims).usingRecursiveComparison().isEqualTo(
            RoninClaims(
                user = RoninUser(
                    id = builder.sekiUserId.toString(),
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
        )
    }

    @Test
    fun `should be successful with seki token for ronin`() {
        val (authValue, builder) = validRoninAuthenticationToken { builder ->
            builder
                .sekiUserId(UUID.randomUUID())
                .sekiEmail("xJU0pmF0Lp@example.com")
                .patientRoninId(null)
                .preferredTimezone(ZoneId.of("America/Chicago"))
                .providerRoninId(null)
                .tenantId("G4ag8L7")
                .tenantName("gAB3y7PP0lJ4D5QgFCaribon")
                .udpId(null)
                .identities(listOf("Elixir.Seki.AuthStrategies.RoninEmployees" to "google-oauth2|1qJYmK09LxjiYHN5DJFzpfHIVpcqWipha4"))
                .firstName("JYq05b")
                .lastName("oaW9Hse")
                .fullName("QCdxZfp16S5ASH")
                .metadata(
                    mapOf(
                        "email" to "ysX9fAesLS@example.com",
                        "family_name" to "xfr3iPA",
                        "given_name" to "N2M06t",
                        "idp" to "qkdJubzw7G7YV7b",
                        "name" to "OoJZuVC2gZh7xy",
                        "sub" to "akKG962rNSqNAKdDSUisCPv2WXWtE8WZLt"
                    )
                )
        }
        assertThat(authValue).isInstanceOfAny(SekiJwtAuthenticationToken::class.java)
        assertThat(authValue.tenantId).isEqualTo(builder.tenantId)
        assertThat(authValue.userId).isEqualTo(builder.sekiUserId.toString())
        assertThat(authValue.udpId).isEqualTo(builder.udpId)
        assertThat(authValue.userFirstName).isEqualTo(builder.firstName)
        assertThat(authValue.userLastName).isEqualTo(builder.lastName)
        assertThat(authValue.userFullName).isEqualTo(builder.fullName)

        assertThat(authValue.roninClaims).usingRecursiveComparison().isEqualTo(
            RoninClaims(
                user = RoninUser(
                    id = builder.sekiUserId.toString(),
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
        )
    }

    private fun validRoninAuthenticationToken(sekiCustomizer: (SekiResponseBuilder) -> SekiResponseBuilder = { it }): Pair<RoninAuthentication, SekiResponseBuilder> {
        val (auth, builder) = validRoninAuthenticationTokenAndRawToken(sekiCustomizer)
        return Pair(auth, builder)
    }

    private fun validRoninAuthenticationTokenAndRawToken(sekiCustomizer: (SekiResponseBuilder) -> SekiResponseBuilder = { it }): Triple<RoninAuthentication, SekiResponseBuilder, String> {
        val decoder = NimbusJwtDecoder.withSecretKey(secretKey(sekiSharedSecret)).build()

        val userId = UUID.randomUUID().toString()
        val token = generateSekiToken(sekiSharedSecret, userId)

        val builder = sekiCustomizer(SekiResponseBuilder(token))
        SimpleSekiMock.successfulValidate(builder)

        return Triple(SekiCustomAuthenticationConverter(sekiClient).convert(decoder.decode(token)) as RoninAuthentication, builder, token)
    }
}
