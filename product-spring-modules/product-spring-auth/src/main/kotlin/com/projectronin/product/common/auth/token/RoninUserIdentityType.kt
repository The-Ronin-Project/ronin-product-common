package com.projectronin.product.common.auth.token

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

sealed class RoninUserIdentityType(val value: String) {

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun forValue(value: String): RoninUserIdentityType = when (value) {
            ProviderUdpId.value -> ProviderUdpId
            ProviderFhirId.value -> ProviderFhirId
            PatientUdpId.value -> PatientUdpId
            PatientSmsId.value -> PatientSmsId
            GoogleAccountId.value -> GoogleAccountId
            M2MClientId.value -> M2MClientId
            MDAAccount.value -> MDAAccount
            else -> Unknown(value)
        }
    }

    object ProviderUdpId : RoninUserIdentityType("PROVIDER_UDP_ID")
    object ProviderFhirId : RoninUserIdentityType("PROVIDER_FHIR_ID")
    object PatientUdpId : RoninUserIdentityType("PATIENT_UDP_ID")
    object PatientSmsId : RoninUserIdentityType("PATIENT_SMS_ID")
    object GoogleAccountId : RoninUserIdentityType("GOOGLE_ACCOUNT_ID")
    object M2MClientId : RoninUserIdentityType("M2M_CLIENT_ID")
    object MDAAccount : RoninUserIdentityType("MDA_ACCOUNT")
    internal class Unknown(value: String) : RoninUserIdentityType(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RoninUserIdentityType) return false

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    @JsonValue
    override fun toString(): String {
        return value
    }
}
