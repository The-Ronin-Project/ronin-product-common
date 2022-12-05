package com.projectronin.product.common.client.adhoc

import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.client.AbstractServiceClient
import com.projectronin.product.common.client.auth.AuthBroker
import com.projectronin.product.common.client.auth.NoOpAuthBroker
import com.projectronin.product.common.client.auth.PassThruAuthBroker
import com.projectronin.product.common.client.auth.PassThruTokenAsCookieAuthBroker
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.util.function.Predicate
import java.util.stream.Collectors

// TODO ******** IMPORTANT ********
//    this file is NOT intended for actual merge into common 'main'
//       it has a temporary lifespan for demonstration purposes!
private const val CLINICIAN_API_URL = "https://clinician-api.dev.projectronin.io/"
private const val CDS_API_URL = "https://clinical-data.dev.projectronin.io"

class ClinicianApiDemoTest {

    private val TENANT_ID = "apposnd"
    private val AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2NTMzMjgzMjAsImlzcyI6IlNla2kiLCJqdGkiOiIycm9zcW05M2VlbmFwYmlrZm8wMXFrODEiLCJzdWIiOiIxNTFhMjUwOS1lNjllLTQwNDMtYmJhOC1kYmY5ODhkZGE1NTUiLCJ0ZW5hbnRpZCI6ImFwcG9zbmQifQ.gmX_Ad6sgTTW0iogI4kwuhYYbnpn5HGIE5RZxi56Ojs"
    private val EXPECTED_USER_ID = "151a2509-e69e-4043-bba8-dbf988dda555"

    private val PATIENT_ID_APPOSND = "eb34dec8-4f5e-49f0-8756-1be822608507"
    private val EXPECTED_PATIENT_NAME_APPOSND = "John Doe567"
    private val PATIENT_ID_PEENG_PEENG = "76fcec86-2dca-4316-9158-0413af7924b4"
    private val EXPECTED_PATIENT_NAME_PEENG = "John Doe Peeng"

    private val directTokenAuth = PassThruAuthBroker(AUTH_TOKEN) // use 'Authorization' header
    private val directTokenCookieAuth = PassThruTokenAsCookieAuthBroker(AUTH_TOKEN) // use 'Cookie' header

    private val clinicianApiClientViaAuth = ClinicianApiClient(CLINICIAN_API_URL, directTokenAuth) // use 'Authorization' header
    private val clinicianApiClientViaCookie = ClinicianApiClient(CLINICIAN_API_URL, directTokenCookieAuth) // use 'Cookie' header
    private val cdsPatientClientViaAuth = CdsPatientClient(CDS_API_URL, directTokenAuth) // use 'Authorization' header
    private val cdsPatientClientViaCookie = CdsPatientClient(CDS_API_URL, directTokenCookieAuth) // use 'Cookie' header

    @Test
    fun `authenticate User Auth Header`() {
        val authResp = clinicianApiClientViaAuth.authenticateUser()
        assertEquals(authResp.user.id, EXPECTED_USER_ID)
        assertEquals(authResp.user.tenantId, TENANT_ID)
        assertEquals(authResp.userSession.tokenString, AUTH_TOKEN)
    }

    @Test
    fun `authenticate User Cookie Header`() {
        val authResp = clinicianApiClientViaCookie.authenticateUser()
        assertEquals(authResp.user.id, EXPECTED_USER_ID)
        assertEquals(authResp.user.tenantId, TENANT_ID)
        assertEquals(authResp.userSession.tokenString, AUTH_TOKEN)
    }

    @Test
    fun `get Patient with Auth Header`() {
        val capiPatient = clinicianApiClientViaAuth.getPatient(PATIENT_ID_APPOSND)
        assertEquals(capiPatient.id, PATIENT_ID_APPOSND, "mismatch expected patient id")
        assertEquals(capiPatient.displayName, EXPECTED_PATIENT_NAME_APPOSND, "mismatch expected patient name")
    }

    @Test
    fun `get Patient with Cookie Header`() {
        val capiPatient = clinicianApiClientViaCookie.getPatient(PATIENT_ID_APPOSND)
        assertEquals(capiPatient.id, PATIENT_ID_APPOSND, "mismatch expected patient id")
        assertEquals(capiPatient.displayName, EXPECTED_PATIENT_NAME_APPOSND, "mismatch expected patient name")
    }

    // Actually get a new token, then attempt to make calls with this new token
    @Test
    fun `authenticate with new token`() {
        val logger = HttpLoggingInterceptor()
        val simpleInterceptor = SimpleRequestInterceptor()
        val cookieJar = SimpleOkHttpCookieJar()
        logger.setLevel(HttpLoggingInterceptor.Level.BODY) // example of lower level logging

        // construct a special okHttpClient that has our special request interceptor
        val okHttpClient: OkHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(simpleInterceptor)
            //.addInterceptor(logger)
            //.addNetworkInterceptor(logger)
            .cookieJar(cookieJar)
            .build()

        // 'pseudo client' b/c need to call 'dev.projectronin.io/seki' (and not 'seki.dev.projectronin.io')
        val sekiClient = PseudoSekiClient("https://dev.projectronin.io/seki", okHttpClient)
        val paramString = "mda_token_flow[provider_origin_id]=&mda_token_flow[patient_origin_id]="

        // call the 'test_mda_token' on the Seki.
        //   don't actually care about the response, just need to get the last forwared request
        //   in order to grab the 'referrer' and 'state' values.
        val testMdaTokenResponse = sekiClient.callTestMdaToken(paramString)
        val lastRequestUrl = simpleInterceptor.requestList.last()
        val httpUrl = lastRequestUrl.toHttpUrl() // using the OkHttp's "HttpUrl" class to parse the url
        val referrer: String = httpUrl.queryParameter("referrer") ?: ""
        val state = httpUrl.queryParameter("state") ?: ""

        val clinicianApiClient = ClinicianApiClient(CLINICIAN_API_URL, NoOpAuthBroker, okHttpClient)
        // NOTE: the response will ALSO set a cookie
        val existingCookieCount = cookieJar.storage.size
        val authResponse = clinicianApiClient.authenticateFromSekiReferrer(referrer, state)
        val newCookieCount = cookieJar.storage.size
        assertEquals(existingCookieCount + 1, newCookieCount, "expected a new cookie after calling 'authenticateFromSekiReferrer'")

        // confirm the token in the auth response is the same as in the returend cookie
        val token = authResponse.userSession.tokenString ?: ""
        val lastCookie = cookieJar.storage.last()
        assertEquals(token, lastCookie.value, "expected token in cookie to match token in auth response")

        // now make some calls using the NEW TOKEN, and confirm everything still works.
        //   only creating a new client b/c this is a test scenario.
        val directTokenCookieAuth = PassThruTokenAsCookieAuthBroker(token)
        val clinicianApiClientUpdated = ClinicianApiClient(CLINICIAN_API_URL, directTokenCookieAuth)

        val authResponseUpdated = clinicianApiClientUpdated.authenticateUser()
        assertEquals(authResponse.user.name, authResponse.user.name, "expected same user new on auth response")

        val fetchedPatient = clinicianApiClientUpdated.getPatient(PATIENT_ID_PEENG_PEENG)
        assertEquals(fetchedPatient.id, PATIENT_ID_PEENG_PEENG, "mismatch expected patient id")
        assertEquals(fetchedPatient.displayName, EXPECTED_PATIENT_NAME_PEENG, "mismatch expected patient name")
    }
}

// //////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////

class ClinicianApiClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient(),
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "ClinicianApiClient/1.0.0"
    }

    fun authenticateFromSekiReferrer(referrer: String, state: String): AuthResponse {
        val extraHeaderMap = mapOf("X-STATE" to state)
        return executePost(requestUrl = "$baseUrl/authenticate?referrer=$referrer", requestPayload = "", extraHeaderMap = extraHeaderMap)
    }

    fun authenticateUser(): AuthResponse {
        return executeGet("$baseUrl/api/user")
    }

    fun getPatient(patientId: String): Patient {
        return executeGet("$baseUrl/api/patients/$patientId")
    }
}

// //////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////
// Note:  called "pseudo seki" becuase ONLY works with "https://dev.projectronin.io/seki",
//    (and _NOT_ "https://seki.dev.projectronin.io")
class PseudoSekiClient(
    hostUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
) :
    AbstractServiceClient(hostUrl, NoOpAuthBroker, client) {
    override fun getUserAgentValue(): String {
        return "PseudoSekiClient/1.0.0"
    }

    fun callTestMdaToken(paramString: String): String {
        val extraHeaderMap = mapOf("Accept" to "*/*")
        return executeGet(requestUrl = "$baseUrl/test_mda_token?$paramString", extraHeaderMap = extraHeaderMap)
    }
}

// //////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////
private const val PATIENT_PATH = "api/patient"
class CdsPatientClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient(),
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "CdsPatientClient/1.0.0"
    }

    fun get(id: String): Patient {
        return executeGet("$baseUrl/$PATIENT_PATH/$id")
    }

    fun create(patient: Patient): Patient {
        return executePost("$baseUrl/$PATIENT_PATH", patient)
    }

    fun delete(id: String): String {
        return executeDelete("$baseUrl/$PATIENT_PATH/$id")
    }
}

// //////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////
//  used to grab the 'last request'...which is only needed because
//     of 'forwarding' the is occuring behind the scenes
class SimpleRequestInterceptor : Interceptor {
    val requestList: MutableList<String> = mutableListOf()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val respRequest = response.request
        // because of "forwarding", want to capture the actual last request (which is on the resposne)
        requestList.add(respRequest.url.toUrl().toString())
        return response
    }
}

// //////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////
//   NOTE: ironically don't 'technically' need the CookieJar, but for reference
//      this is what a simple one looks like that can be utilized in the future.
class SimpleOkHttpCookieJar : CookieJar {
    val storage: MutableList<Cookie> = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        storage.addAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        // Remove expired Cookies
        storage.removeIf(Predicate<Cookie> { cookie: Cookie -> cookie.expiresAt < System.currentTimeMillis() })

        // Only return matching Cookies
        return storage.stream().filter(Predicate<Cookie> { cookie: Cookie -> cookie.matches(url) })
            .collect(Collectors.toList())
    }
}

// //////////////////////////////////////////////////////////////
// //////////////////////////////////////////////////////////////

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
    val updatedAt: Instant? = null,
)

data class PatientTelecom(
    val telecomSystem: String = "",
    val telecomUse: String = "",
    val telecomValue: String = "",
)
