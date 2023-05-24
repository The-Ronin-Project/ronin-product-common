package com.projectronin.product.common.auth

import com.projectronin.product.common.auth.seki.client.model.Identity
import com.projectronin.product.common.auth.seki.client.model.Name
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import com.projectronin.product.common.auth.token.RoninAuthenticationSchemeType
import com.projectronin.product.common.auth.token.RoninUserIdentity
import com.projectronin.product.common.auth.token.RoninUserIdentityType
import com.projectronin.product.common.auth.token.RoninUserType
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class RoninClaimsToSekiDataConverter(
    private val auth: RoninAuthentication
) {

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-DD'T'HH:mm:ss").withZone(ZoneOffset.UTC)
    }

    private val roninClaims = auth.roninClaims

    val sekiUser: User = run {
        when (auth) {
            is SekiJwtAuthenticationToken -> auth.sekiUser
            else -> {
                val maybeUser = roninClaims.user
                User(
                    id = maybeUser?.id ?: "",
                    identities = maybeUser?.identities?.map(::convertIdentity),
                    patientRoninId = maybeUser?.loginProfile?.accessingPatientUdpId,
                    name = Name(firstName = maybeUser?.name?.givenName?.firstOrNull(), fullName = maybeUser?.name?.fullText, lastName = maybeUser?.name?.familyName),
                    preferredTimezone = maybeUser?.preferredTimeZone,
                    providerRoninId = maybeUser?.loginProfile?.accessingProviderUdpId,
                    tenantId = maybeUser?.loginProfile?.accessingTenantId ?: "unknown",
                    tenantName = maybeUser?.loginProfile?.accessingTenantId ?: "unknown",
                    udpId = when (maybeUser?.userType) {
                        RoninUserType.Provider -> maybeUser.loginProfile?.accessingProviderUdpId
                        RoninUserType.Patient -> maybeUser.loginProfile?.accessingPatientUdpId
                        else -> null
                    }
                )
            }
        }
    }

    val sekiSession: UserSession = run {
        when (auth) {
            is SekiJwtAuthenticationToken -> auth.sekiSession
            else -> {
                //  _  __________________________________   __________________________________________________________________________________
                //  X  :idp
                //  X  :ehr_accessing_ronin_patient_id
                //  X  :raw_mda_token_string                TODO: Change auth service to pass actual token string
                //  X  :ehr_accessing_external_patient_id   TODO: Add to actual auth token in auth service
                //  X  :userprovnpi                         TODO: Add this to actual auth token in auth service
                //  X  :userfname
                //  X  :userlname
                //  X  :epicuserid

                val maybeExpiresAt: Instant? = when (auth) {
                    is JwtAuthenticationToken -> auth.token?.expiresAt
                    else -> null
                }
                val maybeOriginalToken: String? = when (auth) {
                    is JwtAuthenticationToken -> auth.token?.tokenValue
                    else -> null
                }

                val metadata = mutableMapOf<String, Any>()
                val maybeUser = roninClaims.user
                metadata += "idp" to when (val schemeType = maybeUser?.authenticationSchemes?.firstOrNull()?.type) {
                    RoninAuthenticationSchemeType.MDAToken, RoninAuthenticationSchemeType.SmartOnFhir -> "ehr"
                    RoninAuthenticationSchemeType.Auth0GoogleOauth, RoninAuthenticationSchemeType.Auth0UsernamePassword -> "ronin_employees"
                    null -> "unknown"
                    else -> schemeType.toString()
                }
                maybeUser?.loginProfile?.accessingPatientUdpId?.let {
                    metadata += "ehr_accessing_ronin_patient_id" to it
                }
                maybeUser?.loginProfile?.accessingExternalPatientId?.let {
                    metadata += "ehr_accessing_external_patient_id" to it
                }
                maybeUser?.identities?.find { it.type == RoninUserIdentityType.MDAEpicUserID }?.id?.let {
                    metadata += "epicuserid" to it
                }
                maybeUser?.identities?.find { it.type == RoninUserIdentityType.MDAUserProvNPI }?.id?.let {
                    metadata += "userprovnpi" to it
                }
                maybeUser?.name?.givenName?.firstOrNull()?.let {
                    if (it.isNotBlank()) {
                        metadata += "userfname" to it
                    }
                }
                maybeUser?.name?.familyName?.let {
                    if (it.isNotBlank()) {
                        metadata += "userlname" to it
                    }
                }
                maybeUser?.authenticationSchemes?.find { it.type == RoninAuthenticationSchemeType.MDAToken }?.id?.let {
                    metadata += "raw_mda_token_string" to it
                }
                UserSession(
                    expiresAt = maybeExpiresAt?.let { dateFormatter.format(it) },
                    metadata = metadata,
                    tokenString = maybeOriginalToken
                )
            }
        }
    }

    private fun convertIdentity(identity: RoninUserIdentity): Identity {
        return Identity(
            authStrategy = when (val identityType = identity.type) {
                RoninUserIdentityType.GoogleAccountId -> sekiRoninEmployeeStrategy
                RoninUserIdentityType.Auth0AccountId -> sekiRoninEmployeeStrategy
                RoninUserIdentityType.PatientSmsId -> sekiOtpPatientStrategy
                RoninUserIdentityType.MDAEpicUserID -> sekiMdaStrategy
                RoninUserIdentityType.ProviderFhirId -> sekiSmartOnFhirStrategy
                else -> identityType.toString()
            },
            externalUserId = identity.id
        )
    }
}
