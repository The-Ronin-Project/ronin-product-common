package com.projectronin.product.common.auth.seki.client

import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.auth.seki.client.model.Identity
import com.projectronin.product.common.auth.seki.client.model.Name
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import com.projectronin.product.common.client.exception.ServiceClientException
import com.projectronin.product.common.test.TestMockHttpClientFactory.createMockClient
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
                identities = listOf(Identity("Foo.Seki.AuthStrategies.MDAToken", "fake_003")),
                name = Name().apply { firstName = "Jane"; lastName = "Doe"; fullName = "Jane Doe" }
            ),
            UserSession()
        )
        private val FAKE_HEALTH_RESPONSE = mapOf("alive" to true)
        private val FAKE_UNHEALTHY_RESPONSE = mapOf("alive" to false)
    }

    @Test
    fun `test valid seki response`() {
        val mockHttpClient = createMockClient(200, FAKE_AUTH_RESPONSE)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val response = sekiClient.validate("token1234")
        assertEquals(FAKE_AUTH_RESPONSE, response)
    }

    @Test
    fun `test seki returns unauthorized`() {
        val mockHttpClient = createMockClient(401, SEKI_INVALID_TOKEN_RESPONSE)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val exception = assertThrows<ServiceClientException> {
            sekiClient.validate("token1234")
        }
        assertEquals(401, exception.getHttpStatusCode(), "mismatch expected httpStatus code")
    }

    // test case where Service is "unauthorized to even connect to Seki"
    //   returns a 401, but this scenario is different from the simple bad token seki response.
    //     (running locally and NOT on the vpn was how the original scenario was found)
    @Test
    fun `test non-seki unauthorized error handling`() {
        val mockHttpClient = createMockClient(401, "Not Authorized to connect to SEKI")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val exception = assertThrows<ServiceClientException> {
            sekiClient.validate("token1234")
        }
        assertEquals(401, exception.getHttpStatusCode(), "mismatched expected httpStatus code")
    }

    @Test
    fun `test seki returns internal error`() {
        val mockHttpClient = createMockClient(500, "Error Response Body")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val exception = assertThrows<ServiceClientException> {
            sekiClient.validate("token1234")
        }
        assertEquals(500, exception.getHttpStatusCode(), "mismatch expected httpStatus code.")
    }

    @Test
    fun `test validate when seki down`() {
        val nestedErrorMessage = "HOST NOT FOUND!"
        val mockHttpClient = createMockClient(UnknownHostException(nestedErrorMessage))
        val sekiClient = SekiClient("https://badurl/", mockHttpClient)

        val exception = assertThrows<ServiceClientException> {
            sekiClient.validate("token1234")
        }
        assertThat(
            "Exception message missing expected substring",
            exception.message, containsString(nestedErrorMessage)
        )
    }

    @Test
    fun `test health is healthy`() {
        val mockHttpClient = createMockClient(200, FAKE_HEALTH_RESPONSE)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val response = sekiClient.health()
        assertEquals(Status.UP, response.status)
    }

    @Test
    fun `test health is unhealthy`() {
        val mockHttpClient = createMockClient(200, FAKE_UNHEALTHY_RESPONSE)
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val response = sekiClient.health()
        assertEquals(Status.DOWN, response.status)
    }

    @Test
    fun `test health check fails`() {
        val mockHttpClient = createMockClient(500, "failure")
        val sekiClient = SekiClient(sekiUrl, mockHttpClient)

        val response = sekiClient.health()
        assertEquals(Status.DOWN, response.status)
        assertEquals("failure", response.details["httpResponse"])
    }

    @Test
    fun `test health seki down`() {
        val nestedErrorMessage = "HOST NOT FOUND!"
        val mockHttpClient = createMockClient(UnknownHostException(nestedErrorMessage))
        val sekiClient = SekiClient("https://badurl/", mockHttpClient)

        val expectedErrorMsg = UnknownHostException::class.java.name + ": " + nestedErrorMessage
        val response = sekiClient.health()
        assertEquals(Status.DOWN, response.status)
        assertEquals(expectedErrorMsg, response.details["error"])
    }
}
