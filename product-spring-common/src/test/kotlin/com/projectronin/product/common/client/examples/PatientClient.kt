package com.projectronin.product.common.client.examples

import com.projectronin.product.common.client.AbstractServiceClient
import com.projectronin.product.common.client.auth.AuthBroker
import okhttp3.OkHttpClient
import java.time.Instant
import java.time.LocalDate

// An example of a client implementation for Patient endpoints on the CDS Service.
//     (obviously this would NOT actually live in the 'common' project, only here as an example)
private const val PATIENT_PATH = "api/patient"
class PatientClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient()
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "PatientClient/1.0.0"
    }

    fun get(id: String): Patient {
        return executeGet("$baseUrl$PATIENT_PATH/$id")
    }

    fun create(patient: Patient): Patient {
        return executePost("$baseUrl$PATIENT_PATH", patient)
    }

    fun delete(id: String) {
        executeRawDelete("$baseUrl$PATIENT_PATH/$id")
    }
}

//
// arguably more ideal to reference a class from the
//   CDS Service Patient rather than make our own 'identical copy of the object
//     But this is used for demonstration purposes.
//        (this class is 'oversimplified' and would be constructed different IRL)
data class Patient(
    val id: String = "",
    val tenantId: String = "",
    val active: Boolean = true,
    val mrn: String = "",
    val udpId: String = "",
    val displayName: String = "",
    val birthSex: String = "",
    val birthDate: LocalDate? = null,
    val telecoms: List<PatientTelecom> = listOf(),
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)

data class PatientTelecom(
    val telecomSystem: String = "",
    val telecomUse: String = "",
    val telecomValue: String = ""
)
