package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.model.Identity
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import com.projectronin.product.common.auth.token.RoninAuthenticationScheme
import com.projectronin.product.common.auth.token.RoninAuthenticationSchemeType
import com.projectronin.product.common.auth.token.RoninClaims
import com.projectronin.product.common.auth.token.RoninLoginProfile
import com.projectronin.product.common.auth.token.RoninName
import com.projectronin.product.common.auth.token.RoninUser
import com.projectronin.product.common.auth.token.RoninUserIdentity
import com.projectronin.product.common.auth.token.RoninUserIdentityType
import com.projectronin.product.common.auth.token.RoninUserType

private const val sekiRoninEmployeeStrategy = "Elixir.Seki.AuthStrategies.RoninEmployees"

private const val sekiOtpPatientStrategy = "Elixir.Seki.AuthStrategies.OtpPatientApp"

private const val sekiMdaStrategy = "Elixir.Seki.AuthStrategies.MDAToken"

private const val sekiSmartOnFhirStrategy = "Elixir.Seki.AuthStrategies.EpicSmartOnFhir"

private const val sekiRoninPatientIdMetadataKey = "ehr_accessing_ronin_patient_id"

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
                loginProfile = loginProfile(),
                identities = sekiUser.identities?.take(5)?.map(::convertIdentity) ?: emptyList(),
                authenticationSchemes = sekiUser.identities?.take(1)?.map(::convertAuthenticationScheme) ?: emptyList()
            )
        )
    }

    private fun userTypeFromIdentities(): RoninUserType {
        return when (val strategy = sekiUser.identities?.firstOrNull()?.authStrategy) {
            sekiRoninEmployeeStrategy -> RoninUserType.RoninEmployee
            sekiOtpPatientStrategy -> RoninUserType.Patient
            sekiMdaStrategy -> RoninUserType.Provider
            sekiSmartOnFhirStrategy -> RoninUserType.Provider
            else -> RoninUserType.forValue(strategy ?: "UNKNOWN")
        }
    }

    private fun convertIdentity(identity: Identity): RoninUserIdentity {
        return RoninUserIdentity(
            type = when (val strategy = sekiUser.identities?.firstOrNull()?.authStrategy) {
                sekiRoninEmployeeStrategy -> RoninUserIdentityType.GoogleAccountId
                sekiOtpPatientStrategy -> RoninUserIdentityType.PatientSmsId
                sekiMdaStrategy -> RoninUserIdentityType.MDAAccount
                sekiSmartOnFhirStrategy -> RoninUserIdentityType.ProviderFhirId
                else -> RoninUserIdentityType.forValue(strategy ?: "UNKNOWN")
            },
            tenantId = tenantId,
            id = identity.externalUserId
        )
    }

    private fun convertAuthenticationScheme(identity: Identity): RoninAuthenticationScheme {
        return RoninAuthenticationScheme(
            type = when (val strategy = sekiUser.identities?.firstOrNull()?.authStrategy) {
                sekiRoninEmployeeStrategy -> RoninAuthenticationSchemeType.Auth0GoogleOauth
                sekiOtpPatientStrategy -> RoninAuthenticationSchemeType.Auth0OTP
                sekiMdaStrategy -> RoninAuthenticationSchemeType.MDAToken
                sekiSmartOnFhirStrategy -> RoninAuthenticationSchemeType.SmartOnFhir
                else -> RoninAuthenticationSchemeType.forValue(strategy ?: "UNKNOWN")
            },
            tenantId = tenantId,
            id = identity.externalUserId
        )
    }

    private fun loginProfile(): RoninLoginProfile {
        val patientRoninIdSource: String? = sekiUser.patientRoninId ?: when (userTypeFromIdentities()) {
            RoninUserType.Patient -> sekiUser.udpId
            else -> null
        }
        return RoninLoginProfile(
            accessingTenantId = tenantId,
            accessingProviderUdpId = sekiUser.providerRoninId,
            accessingPatientUdpId = patientRoninIdSource ?: sekiSession.metadata?.get(sekiRoninPatientIdMetadataKey)?.toString()
        )
    }
}
