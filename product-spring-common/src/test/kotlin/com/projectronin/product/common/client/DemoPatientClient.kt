package com.projectronin.product.common.client

import com.projectronin.product.common.client.auth.AuthBroker
import okhttp3.OkHttpClient
import java.util.UUID

private const val PATIENT_PATH = "api/patient"
class DemoPatientClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient()
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "PatientClient/1.0.0"
    }

    fun get(id: String): DemoPatient {
        return executeGet("$baseUrl$PATIENT_PATH/$id")
    }

    fun create(patient: DemoPatient): DemoPatient {
        return executePost("$baseUrl$PATIENT_PATH", patient)
    }

    fun delete(id: String) {
        executeDelete("$baseUrl$PATIENT_PATH/$id")
    }

    // **************************************************************
    // extra methods Below are for showing alternative uses and/or
    //   used to create special exception handling cases.

    fun getPatientAsString(id: String): String {
        return executeGet("$baseUrl$PATIENT_PATH/$id")
    }

    // method below represents getting a patient response, but then trying to serialize
    //   the response into an object that will NOT work (and thus will throw an exception)
    fun getInvalidPatient(id: String): InvalidPatient {
        return executeGet("$baseUrl$PATIENT_PATH/$id")
    }

    // special method to allow retrieval of response object + flag to NOT throw an exception on 4xx/5xx error
    fun specialGetResponse(id: String): ServiceResponse {
        return executeRequest(makeGetRequest(url = "$baseUrl$PATIENT_PATH/$id", shouldThrowOnStatusError = false))
    }
}

data class InvalidPatient(
    val id: UUID? = null,
    val tenantId: Long = 0L,
    val active: Boolean = true,
    val name: String = ""
)
