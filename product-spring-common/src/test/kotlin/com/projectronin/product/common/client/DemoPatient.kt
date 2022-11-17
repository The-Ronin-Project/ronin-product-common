package com.projectronin.product.common.client

import java.time.Instant
import java.time.LocalDate

data class DemoPatient(
    val id: String = "",
    val tenantId: String = "",
    val active: Boolean = true,
    val mrn: String = "",
    val udpId: String = "",
    val displayName: String = "",
    val birthSex: String = "",
    val birthDate: LocalDate? = null,
    val telecoms: List<DemoPatientTelecom> = listOf(),
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)

data class DemoPatientTelecom(
    val telecomSystem: String = "",
    val telecomUse: String = "",
    val telecomValue: String = ""
)
