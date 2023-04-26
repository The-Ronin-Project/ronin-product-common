package com.projectronin.product.common.auth.seki.client

import com.projectronin.product.common.auth.seki.client.exception.SekiClientException
import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.auth.seki.client.model.Identity
import com.projectronin.product.common.auth.seki.client.model.Name
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import com.projectronin.product.common.config.JsonProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.net.UnknownHostException

private const val SEKI_INVALID_TOKEN_RESPONSE = "{\"error\":\"Unauthorized\"}"

class SekiClientTest {

    companion object {
        private const val sekiUrl = "https://host/seki/"

        private val FAKE_AUTH_RESPONSE = AuthResponse(
            User(
                id = "userId123",
                tenantId = "tenantId456",
                identities = listOf(Identity("Foo.Seki.AuthStrategies.MDAToken", "fake_003")),
                name = Name().apply { firstName = "Jane"; lastName = "Doe"; fullName = "Jane Doe" }
            ),
            UserSession()
        )
        private val FAKE_AUTH_RESPONSE_STRING = com.projectronin.product.common.config.JsonProvider.objectMapper.writeValueAsString(FAKE_AUTH_RESPONSE)
        private const val FAKE_HEALTH_RESPONSE_STRING = """{ "alive": true }"""
        private const val FAKE_UNHEALTHY_RESPONSE_STRING = """{ "alive": false }"""
    }

    @Test
    fun `test valid seki response`() {
        val mockHttpClient = generateMockHttpClient(200, FAKE_AUTH_RESPONSE_STRING)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)

        val response = sekiClient.validate("token1234")
        Assertions.assertEquals(FAKE_AUTH_RESPONSE, response)
    }

    @Test
    fun `test add on slash to baseUrl`() {
        val mockHttpClient = mockk<OkHttpClient>()
        val requestSlot = slot<Request>()
        every { mockHttpClient.newCall(capture(requestSlot)).execute() } throws RuntimeException("error")

        // test if pass in a baseUrl WITHOUT a trailing slash, then it will get appended
        SekiClient("https://host", mockHttpClient, JsonProvider.objectMapper).checkHealth()
        Assertions.assertEquals(requestSlot.captured.url.toString(), "https://host/health")
    }

    @Test
    fun `test seki returns unauthorized`() {
        val mockHttpClient = generateMockHttpClient(401, SEKI_INVALID_TOKEN_RESPONSE)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)

        val exception = assertThrows<SekiClientException> {
            sekiClient.validate("token1234")
        }
        assertThat(exception.message).contains("Token was invalid").overridingErrorMessage("Exception message missing expected substring")
    }

    // test case where AuditService is "unauthorized to even connect to Seki"
    //   returns a 401, but this scenario is different from the simple bad token seki response.
    //     (running locally and NOT on the vpn was how the original scenario was found)
    @Test
    fun `test non-seki unauthorized error handling`() {
        val mockHttpClient = generateMockHttpClient(401, "Not Authorized to connect to SEKI")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)

        val exception = assertThrows<SekiClientException> {
            sekiClient.validate("token1234")
        }
        assertThat(
            exception.message
        ).contains("Unexpected error while fetching token").overridingErrorMessage("Exception message missing expected substring")
    }

    @Test
    fun `test seki returns internal error`() {
        val mockHttpClient = generateMockHttpClient(500, "Error Response Body")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)

        val exception = assertThrows<SekiClientException> {
            sekiClient.validate("token1234")
        }
        assertThat(exception.message).contains("Unexpected error while fetching token").overridingErrorMessage("Exception message missing expected substring")
    }

    @Test
    fun `test validate when seki down`() {
        val mockHttpClient = mockk<OkHttpClient>()
        val nestedErrorMessage = "HOST NOT FOUND!"
        every { mockHttpClient.newCall(any()).execute() } throws UnknownHostException(nestedErrorMessage)
        val sekiClient = SekiClient("https://badurl/", mockHttpClient, JsonProvider.objectMapper)

        val exception = assertThrows<SekiClientException> {
            sekiClient.validate("token1234")
        }
        assertThat(exception.message).contains(nestedErrorMessage).overridingErrorMessage("Exception message missing expected substring")
    }

    @Test
    fun `test health is healthy`() {
        val mockHttpClient = generateMockHttpClient(200, FAKE_HEALTH_RESPONSE_STRING)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)

        val response = sekiClient.checkHealth()
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.health!!.alive).isTrue
    }

    @Test
    fun `test health is unhealthy`() {
        val mockHttpClient = generateMockHttpClient(200, FAKE_UNHEALTHY_RESPONSE_STRING)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)

        val response = sekiClient.checkHealth()
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        assertThat(response.health!!.alive).isFalse
    }

    @Test
    fun `test health check fails`() {
        val mockHttpClient = generateMockHttpClient(500, "failure")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)

        val response = sekiClient.checkHealth()
        assertThat(response.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.health).isNull()
        assertThat(response.rawResponse).isEqualTo("failure")
    }

    @Test
    fun `test health seki down`() {
        val mockHttpClient = mockk<OkHttpClient>()
        val nestedErrorMessage = "HOST NOT FOUND!"
        val unknownHostException = UnknownHostException(nestedErrorMessage)
        every { mockHttpClient.newCall(any()).execute() } throws unknownHostException
        val sekiClient = SekiClient("https://badurl/", mockHttpClient, JsonProvider.objectMapper)

        val expectedErrorMsg = UnknownHostException::class.java.name + ": " + nestedErrorMessage
        val response = sekiClient.checkHealth()
        assertThat(response.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.health).isNull()
        assertThat(response.rawResponse).isNull()
        assertThat(response.exception).isEqualTo(unknownHostException)
    }

    /**
     * Generate mock http client to be used for a single call.
     * @param responseCode the http response Code to be returned on mock execute call
     * @param responseBody the http response body to be returned on mock execute call
     * @return mocked OkHttpClient
     */
    private fun generateMockHttpClient(responseCode: Int, responseBody: String): OkHttpClient {
        val mockHttpClient = mockk<OkHttpClient>()
        val mockHttpResponse = mockk<Response>()
        val mockHttpResponseBody = mockk<ResponseBody>()

        every { mockHttpClient.newCall(any()).execute() } returns mockHttpResponse
        every { mockHttpResponse.code } returns responseCode
        every { mockHttpResponse.body } returns mockHttpResponseBody
        every { mockHttpResponseBody.string() } returns responseBody
        every { mockHttpResponse.close() } returns Unit

        return mockHttpClient
    }
}
