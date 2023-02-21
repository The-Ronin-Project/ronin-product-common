package com.projectronin.product.contracttest

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.projectronin.product.contracttest.wiremocks.SekiResponseBuilder
import com.projectronin.product.contracttest.wiremocks.SimpleSekiMock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(DockerExtension::class)
@Suppress("UsePropertyAccessSyntax")
class DockerExtensionTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            configureFor(DockerExtension.wiremockHost, DockerExtension.wiremockPort)
        }

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(15L, TimeUnit.SECONDS)
            .readTimeout(15L, TimeUnit.SECONDS)
            .build()

        val objectMapper: ObjectMapper by lazy {
            val m = ObjectMapper()
            m.findAndRegisterModules()
            m
        }
    }

    @BeforeEach
    fun setupMethod() {
        resetToDefault()
    }

    @AfterEach
    fun teardownMethod() {
        resetToDefault()
    }

    @Test
    fun shouldCreateAndRetrieveStudent() {
        SimpleSekiMock.successfulValidate(SekiResponseBuilder("FOO"))

        val request = Request.Builder()
            .url("http://${DockerExtension.serviceUrl}/api/student")
            .header("Authorization", "Bearer FOO")
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
            .url("http://${DockerExtension.serviceUrl}/api/student/$studentId")
            .header("Authorization", "Bearer FOO")
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
        SimpleSekiMock.unsuccessfulValidate("FOO")

        val request = Request.Builder()
            .url("http://${DockerExtension.serviceUrl}/api/student")
            .header("Authorization", "Bearer FOO")
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
