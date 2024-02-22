package com.projectronin.product.contracttest

import com.projectronin.product.contracttest.services.ContractTestServiceUnderTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus

/**
 * An example of how to write a local contract test.
 */
@Suppress("DEPRECATION")
@ExtendWith(LocalContractTestExtension::class)
class LocalContractTestExtensionTest {

    /**
     * This is how to get a specific "service definition" out of the extension, in case you need to get data (like port numbers)
     * out of that service
     */
    private fun service(): ContractTestServiceUnderTest = LocalContractTestExtension.serviceOfType()!!

    @BeforeEach
    fun setupMethod() {
        wiremockReset()
    }

    @AfterEach
    fun teardownMethod() {
        wiremockReset()
    }

    @Test
    fun shouldCreateAndRetrieveStudent() = contractTest {
        val request = buildRequest("/api/student") {
            bearerAuthorization(jwtAuthToken())
            post(createStudentRequest.toRequestBody("application/json".toMediaType()))
        }

        val studentId = executeRequest(request, HttpStatus.CREATED) { response ->
            response.readBodyValue<Map<String, Any>>()["id"].toString()
        }
        assertThat(studentId).isNotNull()

        val retrieveStudentRequest = buildRequest("/api/student/$studentId") {
            bearerAuthorization(jwtAuthToken())
        }

        val student = executeRequest(retrieveStudentRequest) { response ->
            response.readBodyValue<Map<String, Any>>()
        }

        assertThat(student).isNotNull()
        assertThat(student["id"]).isEqualTo(studentId)
        assertThat(student["firstName"]).isEqualTo("William")
        assertThat(student["lastName"]).isEqualTo("Doi")
        assertThat(student["favoriteNumber"]).isEqualTo(17)
        assertThat(student["birthDate"]).isEqualTo("2012-12-07")
        assertThat(student["createdAt"]).isNotNull()
        assertThat(student["updatedAt"]).isNotNull()
    }

    @Test
    fun shouldFailOnBadAuth() = contractTest {
        val request = buildRequest("/api/student") {
            bearerAuthorization(invalidJwtAuthToken())
            post(createStudentRequest.toRequestBody("application/json".toMediaType()))
        }
        executeRequest(request, HttpStatus.UNAUTHORIZED) {}
    }
}

private val createStudentRequest = """
    {
        "firstName": "William",
        "lastName": "Doi",
        "favoriteNumber": 17,
        "birthDate": "2012-12-07"
    }
""".trimIndent()
