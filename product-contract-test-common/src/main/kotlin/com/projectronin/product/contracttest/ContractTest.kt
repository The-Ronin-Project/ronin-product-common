package com.projectronin.product.contracttest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.product.common.testutils.AuthMockHelper
import com.projectronin.product.common.testutils.AuthWireMockHelper
import com.projectronin.product.contracttest.services.ContractTestService
import com.projectronin.product.contracttest.services.ContractTestServiceUnderTest
import com.projectronin.product.contracttest.services.ContractTestWireMockService
import com.projectronin.product.contracttest.wiremocks.SekiResponseBuilder
import com.projectronin.product.contracttest.wiremocks.SimpleSekiMock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.util.UUID

/**
 * contractTest is a dsl to make setting up contract tests easier and more readable.
 * Examples are in the `ronin-blueprint` project but basic usage is:
 *
 *  @Test
 *  fun `your test here`() = contractTest {
 *      // do a this. here to see helper methods available to you
 *      // some initial ones to get started with are `buildRequest`, `executeRequest`, 'validAuthToken`, and `invalidAuthToken`
 *  }
 */
fun contractTest(block: ContractTestContext.() -> Unit) {
    ContractTestContext().apply(block)
}

// resets all wiremock stubs
fun wiremockReset() {
    requireNotNull(LocalContractTestExtension.serviceOfType<ContractTestWireMockService>()).reset()
}

class ContractTestContext {
    private val logger = LoggerFactory.getLogger(LocalContractTestExtension.Companion::class.java)

    /**
     * builds a Request object and prefixes the serviceUrl.  You can specify a block to
     * for example: change the request to post, add auth tokens, other headers, or even the url, etc.
     *
     * It defaults to a get
     */
    fun buildRequest(path: String = "", block: Request.Builder.() -> Unit = {}) =
        Request.Builder()
            .url("${serviceUrl}$path")
            .get()
            .apply(block)
            .build()

    /**
     * executes a request based on a specified path, validates the response status code and you specify a response
     * block that can extract the data you want out of the response and do asserts against the response,etc
     *
     * It defaults to a get and expecting a HttpStatus.OK
     */
    fun <T> executeRequest(
        path: String,
        expectedHttpStatus: HttpStatus = HttpStatus.OK,
        responseBlock: (Response) -> T
    ): T = executeRequest(buildRequest(path), expectedHttpStatus, responseBlock)

    /**
     * executes a request, validates the response status code and you specify a response
     * block that can extract the data you want out of the response and do asserts against the response,etc
     *
     * It defaults to a get and expecting a HttpStatus.OK
     */
    fun <T> executeRequest(
        request: Request,
        expectedHttpStatus: HttpStatus = HttpStatus.OK,
        responseBlock: (Response) -> T
    ): T = LocalContractTestExtension.httpClient.newCall(request).also {
        logger.info(request.toString())
    }.execute().use { response ->
        logger.info(response.toString())

        assertThat(HttpStatus.valueOf(response.code)).isEqualTo(expectedHttpStatus)
        response.let(responseBlock)
    }

    /**
     * The url for the service under test
     */
    val serviceUrl: String
        get() = requireNotNull(
            LocalContractTestExtension.serviceOfType() as? ContractTestServiceUnderTest
        ).serviceUrl

    /**
     * returns the default valid auth token
     */
    fun validDefaultAuthToken(
        block: SekiResponseBuilder.() -> Unit = {}
    ): String =
        AuthMockHelper.defaultSekiToken.also {
            SimpleSekiMock.successfulValidate(SekiResponseBuilder(it).apply(block))
        }

    /**
     * returns a valid auth token based optional specified userId and tenantId.  You
     * can provide a block to modify the response to include identities if needed
     */
    fun validAuthToken(
        userId: UUID = UUID.randomUUID(),
        tenantId: String? = null,
        block: SekiResponseBuilder.() -> Unit = {}
    ): String = authToken(userId, tenantId).also {
        SimpleSekiMock.successfulValidate(SekiResponseBuilder(it).apply(block))
    }

    /**
     * returns the default invalid auth token
     */
    fun invalidDefaultAuthToken(): String =
        AuthMockHelper.defaultSekiToken.also {
            SimpleSekiMock.unsuccessfulValidate(it)
        }

    /**
     * returns an invalid auth token based optional specified userId and tenantId.
     */
    fun invalidAuthToken(
        userId: UUID = UUID.randomUUID(),
        tenantId: String? = null
    ): String = authToken(userId, tenantId).also {
        SimpleSekiMock.unsuccessfulValidate(it)
    }

    /**
     *  Sets the Authorization Bearer header on the request.
     */
    fun Request.Builder.bearerAuthorization(token: String): Request.Builder {
        header("Authorization", "Bearer $token")
        return this
    }

    /**
     * convert a contract request class to a RequestBody.  Optionally you can specify a
     * modifyJsonNode block to remove or modify the actual request to test validation etc.
     */
    fun <T> requestBody(
        request: T,
        modifyJsonNode: (JsonNode) -> Unit = {}
    ): RequestBody =
        objectMapper.writeValueAsString(request).run {
            objectMapper.readTree(this)
                .apply(modifyJsonNode)
                .let { objectMapper.writeValueAsString(it) }
        }.toRequestBody("application/json".toMediaType())

    /**
     * returns the body as a string and fails if no body
     */
    fun Response.bodyString() = body?.string() ?: fail("no body")

    /**
     * returns the body as the specified type and fails if no body
     */
    inline fun <reified T> Response.readBodyValue(): T =
        this.body?.readValue<T>() ?: fail("no body")

    /**
     * returns the body as the specified type
     */
    inline fun <reified T> ResponseBody.readValue(): T =
        LocalContractTestExtension.objectMapper.readValue(string())

    /**
     * returns the body as a JsonNode and fails if no body
     */
    fun Response.readBodyTree(): JsonNode =
        this.body?.readTree() ?: fail("no body")

    /**
     * returns the body as a JsonNode
     */
    fun ResponseBody.readTree(): JsonNode =
        objectMapper.readTree(string())

    /**
     * Adds the RoninEmployees identity to the seki response
     */
    fun SekiResponseBuilder.roninEmployee(): SekiResponseBuilder {
        identities(
            listOf("Elixir.Seki.AuthStrategies.RoninEmployees" to "google-oauth2|Nlp3s7Pei0ImMWQG7788DOA7LosLlr")
        )
        return this
    }

    /**
     * removes a field from an object and fails if it's not an object
     */
    fun JsonNode.removeObjectField(fieldName: String) {
        val objectNode = (this as? ObjectNode) ?: fail("not an object")
        objectNode.remove(fieldName)
    }

    /**
     * This is how to get a specific "service definition" out of the extension, in case you need to get data (like port numbers)
     * out of that service
     */
    inline fun <reified T : ContractTestService> serviceOfType(): T? =
        LocalContractTestExtension.serviceOfType()

    private fun authToken(userId: UUID, tenantId: String?) = if (tenantId != null) {
        AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId.toString(), tenantId)
    } else {
        AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId.toString())
    }
}

private val objectMapper = LocalContractTestExtension.objectMapper
