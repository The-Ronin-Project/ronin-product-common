package com.projectronin.product.common.auth.seki.client

import com.projectronin.product.common.auth.seki.client.exception.SekiClientException
import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.auth.seki.client.model.Identity
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import com.projectronin.product.common.config.JsonProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.OkHttpClient
import okhttp3.Request
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.actuate.health.Status
import java.net.UnknownHostException

private const val SEKI_INVALID_TOKEN_RESPONSE = "{\"error\":\"Unauthorized\"}"

class SekiClientTest {

    companion object {
        private const val sekiUrl = "https://host/seki/"

        private val FAKE_AUTH_RESPONSE = AuthResponse(
            User(
                id = "userId123",
                tenantId = "tenantId456",
                udpId = "some-long-string-398091830899-Z",
                identities = listOf(Identity("Foo.Seki.AuthStrategies.MDAToken", "fake_003"))
            ),
            UserSession()
        )
        private val FAKE_AUTH_RESPONSE_STRING = JsonProvider.objectMapper.writeValueAsString(FAKE_AUTH_RESPONSE)
        private const val FAKE_HEALTH_RESPONSE_STRING = """{ "alive": true }"""
        private const val FAKE_UNHEALTHY_RESPONSE_STRING = """{ "alive": false }"""
    }

    @Test
    fun `test valid seki response`() {
        val mockHttpClient = generateMockHttpClient(200, FAKE_AUTH_RESPONSE_STRING)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val response = sekiClient.validate("token1234")
        assertEquals(FAKE_AUTH_RESPONSE, response)
    }

    @Test
    fun `test add on slash to baseUrl`() {
        val mockHttpClient = mockk<OkHttpClient>()
        val requestSlot = slot<Request>()
        every { mockHttpClient.newCall(capture(requestSlot)).execute() } throws RuntimeException("error")

        // test if pass in a baseUrl WITHOUT a trailing slash, then it will get appended
        SekiClient("https://host", mockHttpClient).health()
        assertEquals(requestSlot.captured.url.toString(), "https://host/health")
    }

    @Test
    fun `test seki returns unauthorized`() {
        val mockHttpClient = generateMockHttpClient(401, SEKI_INVALID_TOKEN_RESPONSE)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val exception = assertThrows<SekiClientException> {
            sekiClient.validate("token1234")
        }
        assertThat(
            "Exception message missing expected substring",
            exception.message, containsString("Token was invalid")
        )
    }

    // test case where AuditService is "unauthorized to even connect to Seki"
    //   returns a 401, but this scenario is different from the simple bad token seki response.
    //     (running locally and NOT on the vpn was how the original scenario was found)
    @Test
    fun `test non-seki unauthorized error handling`() {
        val mockHttpClient = generateMockHttpClient(401, "Not Authorized to connect to SEKI")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val exception = assertThrows<SekiClientException> {
            sekiClient.validate("token1234")
        }
        assertThat(
            "Exception message missing expected substring",
            exception.message, containsString("Unexpected error while fetching token")
        )
    }

    @Test
    fun `test seki returns internal error`() {
        val mockHttpClient = generateMockHttpClient(500, "Error Response Body")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val exception = assertThrows<SekiClientException> {
            sekiClient.validate("token1234")
        }
        assertThat(
            "Exception message missing expected substring",
            exception.message, containsString("Unexpected error while fetching token")
        )
    }

    @Test
    fun `test validate when seki down`() {
        val mockHttpClient = mockk<OkHttpClient>()
        val nestedErrorMessage = "HOST NOT FOUND!"
        every { mockHttpClient.newCall(any()).execute() } throws UnknownHostException(nestedErrorMessage)
        val sekiClient = SekiClient("https://badurl/", mockHttpClient)

        val exception = assertThrows<SekiClientException> {
            sekiClient.validate("token1234")
        }
        assertThat(
            "Exception message missing expected substring",
            exception.message, containsString(nestedErrorMessage)
        )
    }

    @Test
    fun `test health is healthy`() {
        val mockHttpClient = generateMockHttpClient(200, FAKE_HEALTH_RESPONSE_STRING)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val response = sekiClient.health()
        assertEquals(Status.UP, response.status)
    }

    @Test
    fun `test health is unhealthy`() {
        val mockHttpClient = generateMockHttpClient(200, FAKE_UNHEALTHY_RESPONSE_STRING)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val response = sekiClient.health()
        assertEquals(Status.DOWN, response.status)
    }

    @Test
    fun `test health check fails`() {
        val mockHttpClient = generateMockHttpClient(500, "failure")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val response = sekiClient.health()
        assertEquals(Status.DOWN, response.status)
        assertEquals("failure", response.details["httpResponse"])
    }

    @Test
    fun `test health seki down`() {
        val mockHttpClient = mockk<OkHttpClient>()
        val nestedErrorMessage = "HOST NOT FOUND!"
        every { mockHttpClient.newCall(any()).execute() } throws UnknownHostException(nestedErrorMessage)
        val sekiClient = SekiClient("https://badurl/", mockHttpClient)

        val expectedErrorMsg = UnknownHostException::class.java.name + ": " + nestedErrorMessage
        val response = sekiClient.health()
        assertEquals(Status.DOWN, response.status)
        assertEquals(expectedErrorMsg, response.details["error"])
    }

    /**
     * Generate mock http client to be used for a single call.
     * @param responseCode the http response Code to be returned on mock execute call
     * @param responseBody the http response body to be returned on mock execute call
     * @return mocked OkHttpClient
     */
    private fun generateMockHttpClient(responseCode: Int, responseBody: String): OkHttpClient {
        val mockHttpClient = mockk<OkHttpClient>()
        val mockHttpResponse = mockk<okhttp3.Response>()
        val mockHttpResponseBody = mockk<okhttp3.ResponseBody>()

        every { mockHttpClient.newCall(any()).execute() } returns mockHttpResponse
        every { mockHttpResponse.code } returns responseCode
        every { mockHttpResponse.body } returns mockHttpResponseBody
        every { mockHttpResponseBody.string() } returns responseBody
        every { mockHttpResponse.close() } returns Unit

        return mockHttpClient
    }
}
