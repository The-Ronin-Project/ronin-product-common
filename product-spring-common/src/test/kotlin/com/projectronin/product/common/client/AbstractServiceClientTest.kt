package com.projectronin.product.common.client

import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.validation.clinical.data.client.work.auth.PassThruAuthBroker
import com.projectronin.validation.clinical.data.client.work.exception.ServiceClientException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.Instant

private const val DEMO_PATIENT_URL = "https://fake.demo.patient.dev.projectronin.io/"
private const val AUTH_TOKEN = "fakeToken"
private val AUTH_BROKER = PassThruAuthBroker(AUTH_TOKEN)
private const val DEMO_PATIENT_USER_AGENT = "PatientClient/1.0.0"

/**
 * Tests for the AbstractServiceClient
 *   Since the class is abstract (obviously), we used a contrived implementation ("DemoPatientClient")
 *     as a concrete instance to test against.
 */
class AbstractServiceClientTest {

    @Test
    fun `basic Get`() {
        val patientId = "12345"
        val displayName = "Jane Doe"
        val mrn = "556677"
        val fakePatientResponse = DemoPatient(
            id = patientId,
            displayName = displayName,
            mrn = mrn,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val mockClient = generateMockHttpClient(200, fakePatientResponse, generateExpectedGetParams(patientId))
        val patientClient = generateDemoClient(mockClient)
        val fetchedPatient = patientClient.get(patientId)

        assertEquals(displayName, fetchedPatient.displayName, "mismatch expected displayName")
    }

    @Test
    fun `basic Create`() {
        val displayName = "Jane Doe"
        val mrn = "556677"
        val patientToCreate = DemoPatient(displayName = displayName, mrn = mrn)

        val fakePatientResponse =
            patientToCreate.copy(id = "_new_patient_id_", createdAt = Instant.now(), updatedAt = Instant.now())

        val mockClient = generateMockHttpClient(201, fakePatientResponse, generateExpectedPostParams(""))
        val patientClient = generateDemoClient(mockClient)

        val createdPatient = patientClient.create(patientToCreate)
        assertEquals(displayName, createdPatient.displayName, "mismatch expected displayName")
    }

    @Test
    fun `basic Delete`() {
        val patientId = "123456789"
        val mockClient = generateMockHttpClient(200, "", generateExpectedDeleteParams(patientId))
        val patientClient = generateDemoClient(mockClient)
        patientClient.delete(patientId)
    }

    // test that client handles trailing url on host param
    //    i.e. client user should NEVER have to worry about if the url looks like:
    //     "https://foo.dev.projectronin.io"  VS  "https://foo.dev.projectronin.io/"
    @Test
    fun `test add on slash to baseUrl`() {
        val hostUrl = "https://host"
        val mockHttpClient = mockk<OkHttpClient>()
        val patientClient = DemoPatientClient(hostUrl, AUTH_BROKER, mockHttpClient)
        assertEquals(hostUrl + "/", patientClient.baseUrl, "expected client baseUrl to append trailing slash '/'")
    }

    @Test
    fun `get not found`() {
        val patientId = "12345"
        val errorStatus = HttpStatus.NOT_FOUND
        // note: this is basically represents how an error response looks like now,
        //   But it is subject to change over time.
        val errorResponse = ErrorResponse(
            httpStatus = errorStatus,
            timestamp = Instant.now(),
            status = errorStatus.value(),
            error = "Not Found",
            exception = "com.projectronin.product.common.exception.NotFoundException",
            message = "Not Found",
            detail = "Item was not found: $patientId"
        )

        val mockClient = generateMockHttpClient(errorResponse.status, errorResponse)
        val patientClient = generateDemoClient(mockClient)

        val exception = assertThrows<ServiceClientException> {
            patientClient.get(patientId)
        }
        assertEquals(errorStatus.value(), exception.getHttpStatusCode(), "mismatch expected error httpStatus on exception")
        // todo: what should "exception.getMessage()" be... the 'message' field from above ??
    }

    /*
        TODO - testcases to add
            Auth - missing token -- will attempt to make a request WITHOUT an Authorization Header
            ** seki - returns 'invalid token'  (not in this class)
            ** seki connection error - unable to call auth   (not in this class)
            .
            Internal Error - returns a 500
            Connection Error - client throws an exception itself  (instead of returning a 4xx or 5xx)
              .. make sure the 'actual excepton' is available - will be the nested 'cause' exception
            Unrecognized Error Response - client returns an error but _NOT_ in the 'ErrorRresposne' object format.
            .
            pass in object for POST that will error when converting it to a string  (a simple 'mock' object will repro)
            test return payload will _NOT_ successfully convert into a 'DemoPatient' resposne.
            .
            Unable to read responseBody (b/c of error)
            Unable to read responseBody (b/c of a 304 or similar)
            .
            GetPayloadByString . validate deserialize works correctly
            PostPayloadByString - validate serialize works correctly
            .
            test with flag shouldTrhowOnStatusError = false
            test protected 'getRaw' methods.. this is for client implementors
            .
            misc - confirm 'close' actually called on internal okHttpResponse
            .
            Test Items NOT going to worry about (for now anyways)
             - payloads that are monsterously big
             - multi-threading tests  (client should not have veriables for this to be an issue, but not writing tests for now)
             - stess/perf/load/etc
     */

    // //////////////////////////////////////////////////

    private fun generateDemoClient(mockHttpClient: OkHttpClient): DemoPatientClient {
        return DemoPatientClient(DEMO_PATIENT_URL, AUTH_BROKER, mockHttpClient)
    }

    // TODO - this was copied from sekiClient
    //    thus will probably move into it's own class so it can be 'reused' by the SekiClient tests
    private fun generateMockHttpClient(
        responseCode: Int,
        responseObject: Any,
        expectedReqValues: ExceptedRequestValues? = null,
    ): OkHttpClient {
        val responseString = convertObjectToString(responseObject)
        val mockHttpResponse = mockk<okhttp3.Response>()
        val mockHttpResponseBody = mockk<okhttp3.ResponseBody>()
        val mockHttpClient = mockk<OkHttpClient>()

        val requestSlot = slot<Request>()

        // NOTE: 'answers' is basically like returns, except it's a lambda
        //   so you can do 'extra stuff' before actually returning the mocked response
        //     in this case validation on the request
        every {
            mockHttpClient.newCall(capture(requestSlot)).execute()
        } answers {
            validateExpectedRequest(requestSlot.captured, expectedReqValues) // run validation on request (as applicable)
            mockHttpResponse // this is the actual 'returns' value
        }

        every { mockHttpResponse.code } returns responseCode
        every { mockHttpResponse.body } returns mockHttpResponseBody
        every { mockHttpResponseBody.string() } returns responseString
        every { mockHttpResponse.close() } returns Unit
        every { mockHttpResponse.headers } returns Headers.headersOf(HttpHeaders.CONTENT_TYPE, "application/json")

        return mockHttpClient
    }

    private fun validateExpectedRequest(request: Request, expectedReqValues: ExceptedRequestValues?) {
        if (expectedReqValues == null) {
            return
        }
        if (expectedReqValues.method != "") {
            assertEquals(expectedReqValues.method, request.method, "mismatch expected request method")
        }
        if (expectedReqValues.requestUrl != "") {
            assertEquals(expectedReqValues.requestUrl, request.url.toString(), "mismatch expected request url")
        }
        if (expectedReqValues.headerMap.isNotEmpty()) {
            // note: only check that the request has all of the expected headers.
            //   we do NOT check if the request has any 'extra' request headers not specified in expected map.
            val requestMap = request.headers.toMap()
            for ((key, value) in expectedReqValues.headerMap) {
                val reqHeaderValue = requestMap.get(key)
                assertEquals(reqHeaderValue, value, "mismatch expected request header value for header '$key'")
            }
        }
    }

    private fun convertObjectToString(inputObject: Any): String {
        return when (inputObject) {
            is String -> inputObject // no conversion needed if input already a string
            else -> JsonProvider.objectMapper.writeValueAsString(inputObject)
        }
    }

    private data class ExceptedRequestValues(
        val method: String = "",
        val requestUrl: String = "",
        val headerMap: Map<String, String> = emptyMap(),
    )

    private fun generateExpectedGetParams(patientId: String): ExceptedRequestValues {
        return ExceptedRequestValues("GET", generateExpectedRequestUrl(patientId), DEFAULT_EXPECTED_REQUEST_HEADERS)
    }

    private fun generateExpectedPostParams(patientId: String): ExceptedRequestValues {
        // todo: post body on request
        return ExceptedRequestValues("POST", generateExpectedRequestUrl(patientId), DEFAULT_EXPECTED_REQUEST_HEADERS)
    }

    private fun generateExpectedDeleteParams(patientId: String): ExceptedRequestValues {
        return ExceptedRequestValues("DELETE", generateExpectedRequestUrl(patientId), DEFAULT_EXPECTED_REQUEST_HEADERS)
    }

    private fun generateExpectedRequestUrl(patientId: String): String {
        return "${DEMO_PATIENT_URL}api/patient" + (if (patientId.isNotEmpty()) { "/$patientId" } else { "" })
    }

    companion object {
        private val DEFAULT_EXPECTED_REQUEST_HEADERS = mapOf(
            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.USER_AGENT to DEMO_PATIENT_USER_AGENT,
            HttpHeaders.AUTHORIZATION to "Bearer $AUTH_TOKEN"
        )
    }
}
