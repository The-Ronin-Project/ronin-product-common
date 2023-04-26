package com.projectronin.product.common.auth.seki.client

import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.auth.seki.client.model.Identity
import com.projectronin.product.common.auth.seki.client.model.Name
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import com.projectronin.product.common.config.JsonProvider
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status
import java.net.UnknownHostException

private const val SEKI_INVALID_TOKEN_RESPONSE = "{\"error\":\"Unauthorized\"}"

class SekiHealthProviderTest {

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
    fun `test health is healthy`() {
        val mockHttpClient = generateMockHttpClient(200, FAKE_HEALTH_RESPONSE_STRING)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)
        val sekiHealthProvider = SekiHealthProvider(sekiClient)

        val response = sekiHealthProvider.health()
        Assertions.assertEquals(Status.UP, response.status)
    }

    @Test
    fun `test health is unhealthy`() {
        val mockHttpClient = generateMockHttpClient(200, FAKE_UNHEALTHY_RESPONSE_STRING)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)
        val sekiHealthProvider = SekiHealthProvider(sekiClient)

        val response = sekiHealthProvider.health()
        Assertions.assertEquals(Status.DOWN, response.status)
    }

    @Test
    fun `test health check fails`() {
        val mockHttpClient = generateMockHttpClient(500, "failure")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient, JsonProvider.objectMapper)
        val sekiHealthProvider = SekiHealthProvider(sekiClient)

        val response = sekiHealthProvider.health()
        Assertions.assertEquals(Status.DOWN, response.status)
        Assertions.assertEquals("failure", response.details["httpResponse"])
    }

    @Test
    fun `test health seki down`() {
        val mockHttpClient = mockk<OkHttpClient>()
        val nestedErrorMessage = "HOST NOT FOUND!"
        every { mockHttpClient.newCall(any()).execute() } throws UnknownHostException(nestedErrorMessage)
        val sekiClient = SekiClient("https://badurl/", mockHttpClient, JsonProvider.objectMapper)
        val sekiHealthProvider = SekiHealthProvider(sekiClient)

        val expectedErrorMsg = UnknownHostException::class.java.name + ": " + nestedErrorMessage
        val response = sekiHealthProvider.health()
        Assertions.assertEquals(Status.DOWN, response.status)
        Assertions.assertEquals(expectedErrorMsg, response.details["error"])
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
