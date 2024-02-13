package com.projectronin.product.contracttest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.product.common.testutils.AuthMockHelper
import com.projectronin.product.common.testutils.AuthWireMockHelper
import com.projectronin.product.common.testutils.RoninWireMockAuthenticationContext
import com.projectronin.product.common.testutils.wiremockJwtAuthToken
import com.projectronin.product.contracttest.database.DeleteBuilder
import com.projectronin.product.contracttest.services.ContractTestKafkaService
import com.projectronin.product.contracttest.services.ContractTestMySqlService
import com.projectronin.product.contracttest.services.ContractTestService
import com.projectronin.product.contracttest.services.ContractTestServiceUnderTest
import com.projectronin.product.contracttest.services.ContractTestWireMockService
import com.projectronin.product.contracttest.services.Topic
import com.projectronin.product.contracttest.wiremocks.SekiResponseBuilder
import com.projectronin.product.contracttest.wiremocks.SimpleSekiMock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.net.URL
import java.sql.Connection
import java.sql.ResultSet
import java.util.Properties
import java.util.UUID
import java.util.concurrent.TimeUnit

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
    ContractTestContext().use { block(it) }
}

suspend fun coContractTest(block: suspend ContractTestContext.() -> Unit) {
    ContractTestContext().use { block(it) }
}

// resets all wiremock stubs
fun wiremockReset() {
    requireNotNull(LocalContractTestExtension.serviceOfType<ContractTestWireMockService>()).reset()
}

class ContractTestContext : AutoCloseable {
    companion object;

    private val logger = LoggerFactory.getLogger(LocalContractTestExtension.Companion::class.java)

    private var sessionToken: String? = null

    private var _httpClient: OkHttpClient? = null

    val httpClient: OkHttpClient
        get() = _httpClient ?: LocalContractTestExtension.httpClient

    val database = Database()
    val kafka = Kafka()

    /**
     * set the token for the session. It will get added to all subsequent buildRequest calls
     */
    fun setSessionToken(token: String) {
        require(token.isNotBlank()) { "must specify a token" }
        sessionToken = token
    }

    /**
     * clears the token for the session
     */
    fun clearSessionToken() {
        sessionToken = null
    }

    fun buildHttpClient(builder: OkHttpClient.Builder = LocalContractTestExtension.httpClient.newBuilder(), block: OkHttpClient.Builder.() -> OkHttpClient.Builder) {
        _httpClient = block(builder).build()
    }

    fun clearHttpClient() {
        _httpClient = null
    }

    /**
     * builds a Request object, defaults to get, prefixes the serviceUrl, ands sets the session bearer auth.
     * You can optionally specify a block to, for example, change the request to post, add auth tokens, other headers,
     * or even the url, etc.
     */
    fun buildRequest(path: String = "", block: Request.Builder.() -> Unit = {}) =
        Request.Builder()
            .url("${serviceUrl}$path")
            .get()
            .apply {
                sessionToken?.also { bearerAuthorization(it) }
            }
            .apply(block)
            .build()

    /**
     * executes a request based on a specified path, validates the response status code. You specify a response
     * block that can extract the data you want out of the response and do assertions against the response,etc
     *
     * It defaults to a get and expecting a [HttpStatus.OK]
     */
    fun <T> executeRequest(
        path: String,
        expectedHttpStatus: HttpStatus = HttpStatus.OK,
        responseBlock: (Response) -> T
    ): T = executeRequest(buildRequest(path), expectedHttpStatus, responseBlock)

    /**
     * executes a request, validates the response status code. You specify a response
     * block that can extract the data you want out of the response and do assertions against the response,etc
     *
     * It defaults to a get and expecting a [HttpStatus.OK]
     */
    fun <T> executeRequest(
        request: Request,
        expectedHttpStatus: HttpStatus = HttpStatus.OK,
        responseBlock: (Response) -> T
    ): T = httpClient.newCall(request).also {
        logger.info(request.toString())
    }.execute().use { response ->
        logger.info(response.toString())

        assertThat(HttpStatus.valueOf(response.code)).isEqualTo(expectedHttpStatus)
        response.let(responseBlock)
    }

    /**
     * execute a bad request, verifies http status and error name, and returns a BadRequest
     * with message and detail.
     */
    fun executeBadRequest(
        request: Request
    ): BadRequest = executeRequest(request, HttpStatus.BAD_REQUEST) {
        it.readBodyTree().let { data ->
            assertThat(data["error"]?.textValue()).isEqualTo("Bad Request")
            BadRequest(
                message = data["message"]?.textValue() ?: "",
                detail = data["detail"]?.textValue() ?: ""
            )
        }
    }

    fun chainRedirects(url: String, label: String = url, expectedStatus: HttpStatus = HttpStatus.FOUND, verifier: (Response) -> Unit): RedirectContext {
        return RedirectContext(this, URL(url), label, expectedStatus, verifier)
    }

    /**
     * The url for the service under test
     */
    val serviceUrl: String
        get() = requireNotNull(
            LocalContractTestExtension.serviceOfType() as? ContractTestServiceUnderTest
        ).serviceUrl

    /**
     * Returns a JWT auth token.  See `AuthWireMockHelper.defaultRoninClaims()` for the defaults
     * that get set into it.  You can pass a block that customizes the code, e.g.:
     *
     * ```
     * val token = jwtAuthToken {
     *     withRsaKey(TEST_RSA_KEY)
     *         .withUserType(RoninUserType.RoninEmployee)
     *         .withScopes("admin:read", "admin:write", "tenant:delete")
     * }
     * ```
     *
     * Note that you will have to be running wiremock (include `ContractTestWireMockService()` in your contract test provider),
     * and configure your service in application-test.yml or application-test.properties to include the wiremock service
     * as an issuer.  E.g.:
     *
     * ```
     * ronin:
     *   auth:
     *     issuers:
     *       - http://127.0.0.1:{{wireMockPort}}
     * ```
     */
    fun jwtAuthToken(block: RoninWireMockAuthenticationContext.() -> Unit = {}): String {
        val issuer = "http://wiremock:8080"
        AuthWireMockHelper.setupMockAuthServerWithRsaKey(
            issuerHost = issuer
        )
        return wiremockJwtAuthToken {
            withIssuer(issuer)
            block(this)
        }
    }

    fun invalidJwtAuthToken(block: RoninWireMockAuthenticationContext.() -> Unit = {}): String {
        return wiremockJwtAuthToken {
            withIssuer("https://example.org")
            block(this)
        }
    }

    /**
     * returns the default valid auth token
     */
    @Deprecated("Seki is being deprecated", replaceWith = ReplaceWith("jwtAuthToken(block)"))
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
    @Deprecated("Seki is being deprecated", replaceWith = ReplaceWith("jwtAuthToken(block)"))
    fun validAuthToken(
        userId: String = UUID.randomUUID().toString(),
        tenantId: String? = null,
        block: SekiResponseBuilder.() -> Unit = {}
    ): String = authToken(userId, tenantId).also {
        SimpleSekiMock.successfulValidate(SekiResponseBuilder(it).apply(block))
    }

    /**
     * returns the default invalid auth token
     */
    @Deprecated("Seki is being deprecated", replaceWith = ReplaceWith("jwtAuthToken(block)"))
    fun invalidDefaultAuthToken(): String =
        AuthMockHelper.defaultSekiToken.also {
            SimpleSekiMock.unsuccessfulValidate(it)
        }

    /**
     * returns an invalid auth token based optional specified userId and tenantId.
     */
    @Deprecated("Seki is being deprecated", replaceWith = ReplaceWith("jwtAuthToken(block)"))
    fun invalidAuthToken(
        userId: String = UUID.randomUUID().toString(),
        tenantId: String? = null
    ): String = authToken(userId, tenantId).also {
        SimpleSekiMock.unsuccessfulValidate(it)
    }

    /**
     *  Sets/replaces the Authorization Bearer header on the request.
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

    /**
     * gets a database connection from the mysql container to clean up database records as needed
     */
    fun getDatabaseConnection(): Connection = database.getConnection()

    /**
     * this will run the specified block of code allowing the builder add records to clean up and
     * at the end do the cleanup.
     *
     * This is a utility function and can be used directly but will generally be used in another function for ease
     * of use in the contract tests like so:
     *
     * fun ContractTestContext.cleanupDatabase(block: (DeleteBuilder) -> Unit) {
     *     cleanupDatabaseWithDeleteBuilder(TenantsDatabaseDeleteBuilder(), block)
     * }
     *
     * and in the test
     *  cleanupDatabase { cleanup ->
     *     <create your record here>.also { cleanup += it }
     *     ...
     *     test code
     *     ...
     *  }
     */
    fun cleanupDatabaseWithDeleteBuilder(builder: DeleteBuilder, block: (DeleteBuilder) -> Unit) =
        database.cleanupWithDeleteBuilder(builder, block)

    private fun authToken(userId: String, tenantId: String?) = if (tenantId != null) {
        AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId, tenantId)
    } else {
        AuthWireMockHelper.generateSekiToken(AuthWireMockHelper.sekiSharedSecret, userId)
    }

    data class BadRequest(
        val message: String,
        val detail: String
    ) {
        fun verifyMissingRequiredField(fieldName: String): BadRequest {
            assertThat(message).isEqualTo("Missing required field '$fieldName'")
            return this
        }

        fun verifyValidationFailure(vararg detailContent: String): BadRequest {
            assertThat(message).isEqualTo("Validation failure")
            assertThat(detail).contains(detailContent.toList())
            return this
        }

        fun verifyInvalidFieldValue(fieldName: String): BadRequest {
            assertThat(message).isEqualTo("Invalid value for field '$fieldName'")
            return this
        }
    }

    inner class Database {
        fun getConnection(): Connection =
            requireNotNull(serviceOfType<ContractTestMySqlService>()) { "mysql service not found" }.createConnection()

        fun executeQuery(sql: String, block: (ResultSet) -> Unit) {
            getDatabaseConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    block(stmt.executeQuery(sql))
                }
            }
        }

        fun executeUpdate(sql: String): Int =
            getDatabaseConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeUpdate(sql)
                }
            }

        fun cleanupWithDeleteBuilder(builder: DeleteBuilder, block: (DeleteBuilder) -> Unit) {
            try {
                block(builder)
            } finally {
                getDatabaseConnection().use { conn ->
                    conn.autoCommit = true
                    builder.build().forEach { sql ->
                        conn.createStatement().use { stmt ->
                            stmt.executeUpdate(sql)
                        }
                    }
                }
            }
        }
    }

    inner class Kafka {
        private fun kafka() = serviceOfType<ContractTestKafkaService>()

        val bootstrapServers: String
            get() = requireNotNull(kafka()) { "no kafka service" }.bootstrapServers

        val topics: List<Topic>
            get() = requireNotNull(kafka()) { "no kafka service" }.topics

        fun <T> producer(
            topic: String,
            valueSerializer: Serializer<T>,
            keyPrefix: String = "",
            properties: Properties = Properties()
        ): TestKafkaProducer<T> {
            properties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
            properties[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = SecurityProtocol.PLAINTEXT.name

            check(topics.any { it.name == topic }) { "topic $topic not registered in ContractServicesProvider" }

            val producer = KafkaProducer(
                properties,
                StringSerializer(),
                valueSerializer
            )

            return TestKafkaProducer(producer, topic, keyPrefix)
        }
    }

    class TestKafkaProducer<T>(
        private val producer: KafkaProducer<String, T>,
        private val topic: String,
        private val keyPrefix: String
    ) : AutoCloseable {
        fun send(key: String, message: T) =
            producer.send(producerRecord(key, message)).get(30L, TimeUnit.SECONDS)

        private fun producerRecord(key: String, message: T) =
            ProducerRecord(topic, "$keyPrefix$key", message)

        override fun close() {
            runCatching {
                with(producer) {
                    flush()
                    close()
                }
            }
        }
    }

    inner class RedirectContext(private val contractTestContext: ContractTestContext, url: URL, label: String, expectedStatus: HttpStatus, verifier: (Response) -> Unit) {

        private val response: Response = run {
            logger.info("Executing redirect chain entry to $label")
            contractTestContext.executeRequest(
                request = contractTestContext.buildRequest {
                    url(url)
                },
                expectedHttpStatus = expectedStatus
            ) {
                verifier(it)
                it
            }
        }

        fun then(label: String? = null, expectedStatus: HttpStatus = HttpStatus.FOUND, urlProvider: (Response) -> URL = { it.redirectUrl }, verifier: (Response) -> Unit): RedirectContext {
            val url = urlProvider(response)
            return RedirectContext(contractTestContext, url, label ?: url.toString(), expectedStatus, verifier)
        }
    }

    override fun close() {
        // TODO cleanup
    }
}

val Response.redirectUrl
    get() = headers["Location"]?.let { URL(it) } ?: fail("Location required in response")

private val objectMapper = LocalContractTestExtension.objectMapper
