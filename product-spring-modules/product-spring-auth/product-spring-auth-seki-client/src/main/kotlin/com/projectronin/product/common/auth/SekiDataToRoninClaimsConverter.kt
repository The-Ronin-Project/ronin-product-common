package com.projectronin.product.common.auth

import com.projectronin.auth.token.RoninAuthenticationScheme
import com.projectronin.auth.token.RoninAuthenticationSchemeType
import com.projectronin.auth.token.RoninClaims
import com.projectronin.auth.token.RoninLoginProfile
import com.projectronin.auth.token.RoninName
import com.projectronin.auth.token.RoninUser
import com.projectronin.auth.token.RoninUserIdentity
import com.projectronin.auth.token.RoninUserIdentityType
import com.projectronin.auth.token.RoninUserType
import com.projectronin.product.common.auth.seki.client.model.Identity
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession

const val sekiRoninEmployeeStrategy = "Elixir.Seki.AuthStrategies.RoninEmployees"

const val sekiOtpPatientStrategy = "Elixir.Seki.AuthStrategies.OtpPatientApp"

const val sekiMdaStrategy = "Elixir.Seki.AuthStrategies.MDAToken"

const val sekiSmartOnFhirStrategy = "Elixir.Seki.AuthStrategies.EpicSmartOnFhir"

const val sekiRoninPatientIdMetadataKey = "ehr_accessing_ronin_patient_id"

const val sekiExternalPatientIdMetadataKey = "ehr_accessing_external_patient_id"

class SekiDataToRoninClaimsConverter(
    val sekiUser: User,
    val sekiSession: UserSession
) {

    val tenantId: String
        get() = sekiUser.tenantId

    val userId: String
        get() = sekiUser.id

    val udpId: String?
        get() = sekiUser.udpId

    val userFirstName: String
        get() = sekiUser.firstName

    val userLastName: String
        get() = sekiUser.lastName

    val userFullName: String
        get() = sekiUser.fullName

    val roninClaims: RoninClaims by lazy {
        RoninClaims(
            RoninUser(
                id = userId,
                userType = userTypeFromIdentities(),
                name = RoninName(
                    fullText = userFullName,
                    givenName = listOf(userFirstName),
                    familyName = userLastName,
                    prefix = emptyList(),
                    suffix = emptyList()
                ),
                preferredTimeZone = sekiUser.preferredTimezone,
                loginProfile = loginProfile(),
                identities = sekiUser.identities?.take(5)?.map(::convertIdentity) ?: emptyList(),
                authenticationSchemes = sekiUser.identities?.take(1)?.map(::convertAuthenticationScheme) ?: emptyList()
            )
        )
    }

    internal fun <T> matchOnIdentity(
        googleAccountValue: T,
        usernamePasswordValue: T,
        patientValue: T,
        mdaValue: T,
        sofValue: T,
        unknownBuilder: (String) -> T
    ): T {
        val strategy = sekiUser.identities?.firstOrNull()
        return when (val strategyName = strategy?.authStrategy) {
            sekiRoninEmployeeStrategy -> {
                if (strategy.externalUserId?.startsWith("google-oauth2|") == true) {
                    googleAccountValue
                } else {
                    usernamePasswordValue
                }
            }

            sekiOtpPatientStrategy -> patientValue
            sekiMdaStrategy -> mdaValue
            sekiSmartOnFhirStrategy -> sofValue
            else -> unknownBuilder(strategyName ?: "UNKNOWN")
        }
    }

    internal fun userTypeFromIdentities(): RoninUserType {
        return matchOnIdentity(
            googleAccountValue = RoninUserType.RoninEmployee,
            usernamePasswordValue = RoninUserType.IntegrationTest,
            patientValue = RoninUserType.Patient,
            mdaValue = RoninUserType.Provider,
            sofValue = RoninUserType.Provider,
            unknownBuilder = RoninUserType::forValue
        )
    }

    internal fun convertIdentity(identity: Identity): RoninUserIdentity {
        return RoninUserIdentity(
            type = matchOnIdentity(
                googleAccountValue = RoninUserIdentityType.GoogleAccountId,
                usernamePasswordValue = RoninUserIdentityType.Auth0AccountId,
                patientValue = RoninUserIdentityType.PatientSmsId,
                mdaValue = RoninUserIdentityType.MDAEpicUserID,
                sofValue = RoninUserIdentityType.ProviderFhirId,
                unknownBuilder = RoninUserIdentityType::forValue
            ),
            tenantId = tenantId,
            id = identity.externalUserId
        )
    }

    internal fun convertAuthenticationScheme(identity: Identity): RoninAuthenticationScheme {
        return RoninAuthenticationScheme(
            type = matchOnIdentity(
                googleAccountValue = RoninAuthenticationSchemeType.Auth0GoogleOauth,
                usernamePasswordValue = RoninAuthenticationSchemeType.Auth0UsernamePassword,
                patientValue = RoninAuthenticationSchemeType.Auth0OTP,
                mdaValue = RoninAuthenticationSchemeType.MDAToken,
                sofValue = RoninAuthenticationSchemeType.SmartOnFhir,
                unknownBuilder = RoninAuthenticationSchemeType::forValue
            ),
            tenantId = tenantId,
            id = identity.externalUserId
        )
    }

    internal fun loginProfile(): RoninLoginProfile {
        val patientRoninIdSource: String? = sekiUser.patientRoninId ?: when (userTypeFromIdentities()) {
            RoninUserType.Patient -> sekiUser.udpId
            else -> null
        }
        return RoninLoginProfile(
            accessingTenantId = tenantId,
            accessingProviderUdpId = sekiUser.providerRoninId,
            accessingPatientUdpId = patientRoninIdSource ?: sekiSession.metadata?.get(sekiRoninPatientIdMetadataKey)?.toString(),
            accessingExternalPatientId = sekiSession.metadata?.get(sekiExternalPatientIdMetadataKey)?.toString()
        )
    }
}
