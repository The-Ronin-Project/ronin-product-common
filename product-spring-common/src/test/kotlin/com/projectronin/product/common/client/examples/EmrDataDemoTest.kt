package com.projectronin.product.common.client.examples

import com.projectronin.product.common.client.AbstractServiceClient
import com.projectronin.validation.clinical.data.client.work.auth.AuthBroker
import com.projectronin.validation.clinical.data.client.work.auth.PassThruAuthBroker
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

private const val EMR_DATA_URL = "https://emr-data.stage.projectronin.io/"
private const val AUTH_TOKEN = ""

// TODO - remove this class before actual checkin
class EmrDataDemoTest {

    // Example for EmrDataService is temporary (and commented out)
    //   this relies on the "Event Patient" auto-generated classes
    //     that are not available in this project

    /*
    @Disabled
    @Test
    fun executeEmrDataDemo() {
        // create new Emr Data Client
        val emrDataClient = EmrDataClient(EMR_DATA_URL, PassThruAuthBroker(AUTH_TOKEN))

        val patientId = "demo-eSJfpWwm7oYpecHoTmtq6-SMk5gB7-4DMzVlW52FDBMc3"

        // Query for the "Event form" of the Patient
        val patient = emrDataClient.getEventPatient(patientId)
        println("Patient fhirId: ${patient.fhirId}")
        println(patient)
    }
     */
}

// /////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////
// Below is how to implement an Emr Data Client to be used for REST calls
//     This is for demo purposes and would ultimately find a 'better home'
//
/*
private const val EMR_DATA_PATH = "api/patient" // <-- subject to change
class EmrDataClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient()
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "EmrDataClient/1.0.0"
    }

    fun getEventPatient(id: String): com.projectronin.product.common.client.examples.eventpatient.Patient {
        return executeGet("$baseUrl$EMR_DATA_PATH/event/$id")
    }
}
*/
