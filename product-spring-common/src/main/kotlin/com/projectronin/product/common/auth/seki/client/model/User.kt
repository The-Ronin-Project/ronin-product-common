package com.projectronin.product.common.auth.seki.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val id: String = "",
    val identities: List<Identity>? = null,
    val patientRoninId: String? = null,
    val name: Name? = null,
    val preferredTimezone: String? = null, // could add special marshalling to make ZoneId
    val providerRoninId: String? = null,
    val tenantId: String = "",
    val tenantName: String? = null,
    val udpId: String? = null
) {
    val firstName: String
        get() = name?.firstName ?: ""
    val lastName: String
        get() = name?.lastName ?: ""
    val fullName: String
        get() = name?.fullName ?: ""
}
