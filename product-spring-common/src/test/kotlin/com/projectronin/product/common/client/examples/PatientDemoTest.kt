package com.projectronin.product.common.client.examples

import com.projectronin.product.common.client.AbstractServiceClient
import com.projectronin.validation.clinical.data.client.work.auth.AuthBroker
import com.projectronin.validation.clinical.data.client.work.auth.PassThruAuthBroker
import com.projectronin.validation.clinical.data.client.work.exception.ServiceClientException
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

private const val CDS_URL = "https://clinical-data.dev.projectronin.io"
private const val AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2NTMzMjgzMjAsImlzcyI6IlNla2kiLCJqdGkiOiIycm9zcW05M2VlbmFwYmlrZm8wMXFrODEiLCJzdWIiOiIxNTFhMjUwOS1lNjllLTQwNDMtYmJhOC1kYmY5ODhkZGE1NTUiLCJ0ZW5hbnRpZCI6ImFwcG9zbmQifQ.gmX_Ad6sgTTW0iogI4kwuhYYbnpn5HGIE5RZxi56Ojs"

class PatientDemoTest {
    /**
     * Example how to make 'real calls' to the CDS Service on Patient endpoint
     *   This is more for 'demonstration purposes' rather than what an exact testCase would look like.
     */
    @Disabled
    @Test
    fun executePatientDemo() {
        // create new Patient Client
        val patientClient = PatientClient(CDS_URL, PassThruAuthBroker(AUTH_TOKEN))

        val patientToCreate = Patient(
            displayName = "Robert Paulson",
            tenantId = "mdaocFake",
            mrn = "12346789",
            birthSex = "M",
            birthDate = LocalDate.of(1950, 4, 29)
        )

        // lets make call to create patient
        val createdPatient = patientClient.create(patientToCreate)
        println("Patient was created with id: '${createdPatient.id}'")

        // lets make a call to fetch the patient that was created
        val fetchedPatient = patientClient.get(createdPatient.id)
        println("Retrieved patient with name: '${fetchedPatient.displayName}' and id: '${fetchedPatient.id}'")

        // lets make a call to delete the patient
        try {
            patientClient.delete(createdPatient.id)
        } catch (e: ServiceClientException) {
            // ignore for now!   --- there is a bug with delete!!
            e.printStackTrace()
        }

        // let make a call to fetch the patient (again)
        //   patient no longer exists so we should get a 404 Exception
        try {
            patientClient.get(createdPatient.id)
            fail("Expected 'not found' exception to be thrown when fetching a patient that doesn't exist")
        } catch (e: ServiceClientException) {
            assertEquals(404, e.getHttpStatusCode())
        }
    }
}

// /////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////
// Below is how to implement a CDS Patient Client to be used for REST calls on Patient endpoint
//     This is for demo purposes and would ultimately find a 'better home'
//
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
