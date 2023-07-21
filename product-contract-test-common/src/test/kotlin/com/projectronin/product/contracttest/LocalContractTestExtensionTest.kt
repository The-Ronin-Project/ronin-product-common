package com.projectronin.product.contracttest

import com.projectronin.product.common.testutils.AuthMockHelper
import com.projectronin.product.contracttest.LocalContractTestExtension.Companion.httpClient
import com.projectronin.product.contracttest.LocalContractTestExtension.Companion.objectMapper
import com.projectronin.product.contracttest.services.ContractTestServiceUnderTest
import com.projectronin.product.contracttest.services.ContractTestWireMockService
import com.projectronin.product.contracttest.wiremocks.SekiResponseBuilder
import com.projectronin.product.contracttest.wiremocks.SimpleSekiMock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * An example of how to write a local contract test.
 */
@ExtendWith(LocalContractTestExtension::class)
class LocalContractTestExtensionTest {

    /**
     * This is how to get a specific "service definition" out of the extension, in case you need to get data (like port numbers)
     * out of that service
     */
    private fun wiremock(): ContractTestWireMockService = LocalContractTestExtension.serviceOfType()!!

    private fun service(): ContractTestServiceUnderTest = LocalContractTestExtension.serviceOfType()!!

    @BeforeEach
    fun setupMethod() {
        // resets the wiremock stubs before each test
        wiremock().reset()
    }

    @AfterEach
    fun teardownMethod() {
        // resets the wiremock stubs after each test
        wiremock().reset()
    }

    @Test
    fun shouldCreateAndRetrieveStudent() {
        SimpleSekiMock.successfulValidate(SekiResponseBuilder(AuthMockHelper.defaultSekiToken))

        val request = Request.Builder()
            .url("${service().serviceUrl}/api/student")
            .header("Authorization", "Bearer ${AuthMockHelper.defaultSekiToken}")
            .post(
                """
                    {
                        "firstName": "William",
                        "lastName": "Doi",
                        "favoriteNumber": 17,
                        "birthDate": "2012-12-07"
                    }
                """.trimIndent()
                    .toRequestBody("application/json".toMediaType())
            )
            .build()
        val studentId = httpClient.newCall(request).execute().use { response ->
            assertThat(response.code).isEqualTo(201)
            val body = response.body?.string()
            assertThat(body).isNotNull()
            objectMapper.readValue(body!!, Map::class.java)["id"].toString()
        }
        assertThat(studentId).isNotNull()

        val retrieveStudentRequest = Request.Builder()
            .url("${service().serviceUrl}/api/student/$studentId")
            .header("Authorization", "Bearer ${AuthMockHelper.defaultSekiToken}")
            .get()
            .build()

        httpClient.newCall(retrieveStudentRequest).execute().use { response ->
            assertThat(response.code).isEqualTo(200)
            val body = response.body?.string()
            assertThat(body).isNotNull()
            val student = objectMapper.readValue(body, Map::class.java)
            assertThat(student).isNotNull()
            assertThat(student["id"]).isEqualTo(studentId)
            assertThat(student["firstName"]).isEqualTo("William")
            assertThat(student["lastName"]).isEqualTo("Doi")
            assertThat(student["favoriteNumber"]).isEqualTo(17)
            assertThat(student["birthDate"]).isEqualTo("2012-12-07")
            assertThat(student["createdAt"]).isNotNull()
            assertThat(student["updatedAt"]).isNotNull()
        }
    }

    @Test
    fun shouldFailOnBadAuth() {
        SimpleSekiMock.unsuccessfulValidate(AuthMockHelper.defaultSekiToken)

        val request = Request.Builder()
            .url("${service().serviceUrl}/api/student")
            .header("Authorization", "Bearer ${AuthMockHelper.defaultSekiToken}")
            .post(
                """
                    {
                        "firstName": "William",
                        "lastName": "Doi",
                        "favoriteNumber": 17,
                        "birthDate": "2012-12-07"
                    }
                """.trimIndent()
                    .toRequestBody("application/json".toMediaType())
            )
            .build()
        httpClient.newCall(request).execute().use { response ->
            assertThat(response.code).isEqualTo(401)
        }
    }
}
