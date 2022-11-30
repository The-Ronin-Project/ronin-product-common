package com.projectronin.product.common.client

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.product.common.client.auth.PassThruAuthBroker
import com.projectronin.product.common.client.exception.ServiceClientException
import com.projectronin.product.common.config.JsonProvider.objectMapper
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.test.ExceptedRequestValues
import com.projectronin.product.common.test.TestMockHttpClientFactory.createMockClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.net.UnknownHostException
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

        val mockClient = createMockClient(200, fakePatientResponse, generateExpectedGetParams(patientId))
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

        val mockClient = createMockClient(201, fakePatientResponse, generateExpectedPostParams(""))
        val patientClient = generateDemoClient(mockClient)

        val createdPatient = patientClient.create(patientToCreate)
        assertEquals(displayName, createdPatient.displayName, "mismatch expected displayName")
    }

    @Test
    fun `basic Delete`() {
        val patientId = "123456789"
        val mockClient = createMockClient(200, "", generateExpectedDeleteParams(patientId))
        val patientClient = generateDemoClient(mockClient)
        patientClient.delete(patientId)
    }

    // use alternate method on client to test getting the response as basic string (instead of an object)
    @Test
    fun `get response as string`() {
        val patientId = "12345"
        val fakePatientResponse = DemoPatient(
            id = patientId,
            displayName = "Jane Doe",
            mrn = "556677",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val mockClient = createMockClient(200, fakePatientResponse, generateExpectedGetParams(patientId))
        val patientClient = generateDemoClient(mockClient)
        val fetchedPatientString = patientClient.getPatientAsString(patientId)

        // the response string should be a valid json payload response.
        //   Convert it into a patient and confirm it is as expected
        val convertedResponse: DemoPatient = objectMapper.readValue<DemoPatient>(fetchedPatientString)
        assertEquals(fakePatientResponse, convertedResponse, "mismatch expected patient generated from string response")
    }

    // test that client handles trailing slash on host url param
    //   i.e. client user should _NEVER_ have to worry about if the url looks like:
    //     "https://foo.dev.projectronin.io"  VS  "https://foo.dev.projectronin.io/"
    @Test
    fun `test add on slash to baseUrl`() {
        val hostUrl = "https://host/"
        val mockHttpClient = mockk<OkHttpClient>()
        val patientClient = DemoPatientClient(hostUrl, AUTH_BROKER, mockHttpClient)
        assertEquals("https://host", patientClient.baseUrl, "expected client baseUrl to remove any trailing slash '/'")
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

        val mockClient = createMockClient(errorResponse.status, errorResponse)
        val patientClient = generateDemoClient(mockClient)

        val exception = assertThrows<ServiceClientException> {
            patientClient.get(patientId)
        }
        assertEquals(errorStatus.value(), exception.getHttpStatusCode(), "mismatch expected error httpStatus on exception")
        // todo: what should "exception.getMessage()" be... the 'message' field from above ??
    }

    // test case where the client call throw an exception (was unable to even make the request)
    @Test
    fun `connection error`() {
        val testException = UnknownHostException("HOST NOT FOUND!")
        val mockClient = createMockClient(testException)
        val patientClient = generateDemoClient(mockClient)

        val exception = assertThrows<ServiceClientException> {
            patientClient.get("12345")
        }
        val causeException = exception.cause
        assertNotNull(causeException, "expected exception to have a nested cause exception")
        assertEquals(testException, causeException, "mismatch expected cause exception")
    }

    // when calling 'Create', the input parameter is converted into a string to be the POST request Body.
    //    Test when the conversion of the object to string causes an error.
    @Test
    fun `submit Create with invalid object`() {
        // use a mock object to pass in to the create, b/c know this will blow up
        //   if try to serialize it to a JSON string.
        val bogusPatientObject = mockk<DemoPatient>()

        // input params to the mock won't matter in this case.
        val mockClient = createMockClient(200, "")
        val patientClient = generateDemoClient(mockClient)

        val exception = assertThrows<ServiceClientException> {
            val fetchedPatient = patientClient.create(bogusPatientObject)
        }

        assertThat("Exception message missing expected substring", exception.message, containsString("Unable to serialize"))
        assertNotNull(exception.cause, "expected not null nested cause exception.")
    }

    // call GET, retrieve a valid response, but then try to deserialize into an object that is incompatible
    //   should throw appropriate exception
    @Test
    fun `invalid deserialize of a valid response`() {
        val patientId = "12345"
        val displayName = "Jane Doe"
        val mrn = "556677"
        // the valid response to be returned.
        val fakePatientResponse = DemoPatient(
            id = patientId,
            displayName = displayName,
            mrn = mrn,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val mockClient = createMockClient(200, fakePatientResponse, generateExpectedGetParams(patientId))
        val patientClient = generateDemoClient(mockClient)

        // attempting to deserialize a response into incompatible object should throw exception
        val exception = assertThrows<ServiceClientException> {
            val fetchedPatient = patientClient.getInvalidPatient(patientId)
        }

        assertThat("Exception message missing expected substring", exception.message, containsString("Unable to deserialize"))
        val causeException = exception.cause
        assertNotNull(causeException, "expected not null nested cause exception.")
        assertEquals(InvalidFormatException::class, causeException!!::class, "mismatch expected cause exception type")
    }

    // Test when get back an http status response error, but the body is _NOT_ in the ErrorResponse form
    //    the exception should still have a semi-populated errorResponse object
    @Test
    fun `handle unknown error response format`() {
        val errorResponseBody = "A BIG ERROR!"
        val errorCode = 500
        val mockClient = createMockClient(errorCode, errorResponseBody)
        val patientClient = generateDemoClient(mockClient)

        val exception = assertThrows<ServiceClientException> {
            patientClient.get("123")
        }
        assertEquals(errorCode, exception.getHttpStatusCode(), "mismatch error http status code on exception")
        assertNotNull(exception.errorResponse, "expected exception to have an errorResponse object")
        assertEquals(errorResponseBody, exception.errorResponse!!.detail, "expected raw error resonse in exception detial")
    }

    // call special method that ill return the response object and will _NOT_ throw on a 4xx/5xx error
    @Test
    fun `call special method withou exception`() {
        val errorResponseBody = "A BIG ERROR!"
        val errorCode = 500
        val mockClient = createMockClient(errorCode, errorResponseBody)
        val patientClient = generateDemoClient(mockClient)

        // this should _NOT_ throw an exception (because of special defined test method definition)
        val serviceResponse = patientClient.specialGetResponse("232")
        assertEquals(errorCode, serviceResponse.httpCode, "mismatch serviceResponse http error code")
        assertEquals(errorResponseBody, serviceResponse.body, "mismatch serviceResponse body text")
    }

    // Test to confirm that 'close()' was called on the internal httpClient response
    @Test
    fun `confirm response closed`() {
        val mockHttpResponse = mockk<Response>()
        val mockHttpResponseBody = mockk<ResponseBody>()
        val mockHttpClient = mockk<OkHttpClient>()
        every { mockHttpClient.newCall(any()).execute() } returns mockHttpResponse
        every { mockHttpResponse.code } returns 200
        every { mockHttpResponse.body } returns mockHttpResponseBody
        every { mockHttpResponseBody.string() } returns "abcd"
        every { mockHttpResponse.close() } returns Unit
        every { mockHttpResponse.headers } returns Headers.headersOf(HttpHeaders.CONTENT_TYPE, "application/json")

        val patientClient = generateDemoClient(mockHttpClient)
        val responseString = patientClient.getPatientAsString("1234")

        // make sure the response was closed (avoid potential connection leaks)
        verify(exactly = 1) { mockHttpResponse.close() }
    }

    // if don't pass in a any httpClient (or mockClient), then the normal default httpClient should be used
    //   this would be normal for most non-unittest cases
    @Test
    fun `validate default http client`() {
        // normal client creation  (no mock included)
        val patientClient = DemoPatientClient(DEMO_PATIENT_URL, AUTH_BROKER)

        // this will try to make an actual network call (and should fail!)
        val exception = assertThrows<ServiceClientException> {
            val fetchedPatient = patientClient.get("1234")
        }

        val causeException = exception.cause
        assertNotNull(causeException, "expected exception to have nested cause")
        assertEquals(UnknownHostException::class, causeException!!::class, "mismatch expected cause exception type")
    }

    // ******************************************************
    // todo: ponder cleanup below

    private fun generateDemoClient(mockHttpClient: OkHttpClient): DemoPatientClient {
        return DemoPatientClient(DEMO_PATIENT_URL, AUTH_BROKER, mockHttpClient)
    }

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
