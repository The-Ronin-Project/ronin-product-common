package com.projectronin.product.common.client.adhoc

import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.client.AbstractServiceClient
import com.projectronin.product.common.client.auth.AuthBroker
import com.projectronin.product.common.client.auth.PassThruAuthBroker
import com.projectronin.product.common.client.auth.PassThruTokenAsCookieAuthBroker
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

// TODO ******** IMPORTANT ********
//    this file is NOT intended for actual merge into common 'main'
//       it has a temporary lifespan for demonstration purposes!

private const val SEKI_URL = "https://seki.dev.projectronin.io/"
private const val AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2NTMzMjgzMjAsImlzcyI6IlNla2kiLCJqdGkiOiIycm9zcW05M2VlbmFwYmlrZm8wMXFrODEiLCJzdWIiOiIxNTFhMjUwOS1lNjllLTQwNDMtYmJhOC1kYmY5ODhkZGE1NTUiLCJ0ZW5hbnRpZCI6ImFwcG9zbmQifQ.gmX_Ad6sgTTW0iogI4kwuhYYbnpn5HGIE5RZxi56Ojs"
private const val CDS_API_URL = "https://clinical-data.dev.projectronin.io"
private const val CLINICIAN_API_URL = "https://clinician-api.dev.projectronin.io/"

class ClinicianApiDemoTest {
    @Disabled
    @Test
    fun executeClinicianApiCalls() {

        val directTokenAuth = PassThruAuthBroker(AUTH_TOKEN) // use 'Authorization' header
        val directTokenCookieAuth = PassThruTokenAsCookieAuthBroker(AUTH_TOKEN) // use 'Cookie' header

        val cdsPatientClientA = CdsPatientClient(CDS_API_URL, directTokenAuth) // use 'Authorization' header
        val cdsPatientClientB = CdsPatientClient(CDS_API_URL, directTokenCookieAuth) // use 'Cookie' header

        val clinicianApiClientA = ClinicianApiClient(CLINICIAN_API_URL, directTokenAuth) // use 'Authorization' header
        val clinicianApiClientB = ClinicianApiClient(CLINICIAN_API_URL, directTokenCookieAuth) // use 'Cookie' header

        val patientId = "eb34dec8-4f5e-49f0-8756-1be822608507"

        try {
            // call CAPI /authenticate endpoint
            //    (  currently not avaiable  )

            // call CAPI /api/user endpoint
            val authRespA = clinicianApiClientA.authenticateFromCookie()
            val authRespB = clinicianApiClientB.authenticateFromCookie()

            // query patient from CDS directly with 'Authorization' header
            val cdsPatientA = cdsPatientClientA.get(patientId)

            // query patient from CDS directly with 'Cookie' header
            val cdsPatientB = cdsPatientClientB.get(patientId)

            // query patient from CAPI with 'Authorization' header
            val capiPatientA = clinicianApiClientA.getPatient(patientId)

            // query patient from CAPI with 'Cookie' header
            val capiPatientB = clinicianApiClientB.getPatient(patientId)

            println("SUCCESS!")
        } catch (e: Exception) {
            e.printStackTrace()
            println("ERROR: ${e.message}")
            println("done")
        }
    }
}

// //////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////

private class ClinicianApiClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient()
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "ClinicianApiClient/1.0.0"
    }

    fun authenticateFromSekiReferrer(referrer: String, state: String): AuthResponse {
        val extraHeaderMap = mapOf("X-STATE" to state)
        return executePost(requestUrl = "$baseUrl/authenticate?referrer=$referrer", requestPayload = "", extraHeaderMap = extraHeaderMap)
    }

    fun authenticateFromCookie(): AuthResponse {
        return executeGet("$baseUrl/api/user")
    }

    fun getPatient(patientId: String): Patient {
        return executeGet("$baseUrl/api/patients/$patientId")
    }
}

// An example of a client implementation for Patient endpoints on the CDS Service.
//     (obviously this would NOT actually live in the 'common' project, only here as an example)
private const val PATIENT_PATH = "api/patient"
private class CdsPatientClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient()
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "CdsPatientClient/1.0.0"
    }

    fun get(id: String): Patient {
        return executeGet("$baseUrl/$PATIENT_PATH/$id")
    }

    fun create(patient: com.projectronin.product.common.client.examples_backup.Patient): Patient {
        return executePost("$baseUrl/$PATIENT_PATH", patient)
    }

    fun delete(id: String) {
        executeDelete("$baseUrl/$PATIENT_PATH/$id")
    }
}

// //////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////

private data class Patient(
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

private data class PatientTelecom(
    val telecomSystem: String = "",
    val telecomUse: String = "",
    val telecomValue: String = ""
)
