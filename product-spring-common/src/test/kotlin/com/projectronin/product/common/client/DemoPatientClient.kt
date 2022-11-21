package com.projectronin.product.common.client

import com.projectronin.product.common.client.auth.AuthBroker
import okhttp3.OkHttpClient

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

    // extra methods for testing that also show alternative usages
}
