package com.projectronin.product.common.webfluxtest

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.ninjasquad.springmockk.MockkBean
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.exception.SekiInvalidTokenException
import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.auth.seki.client.model.Name
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import com.projectronin.product.common.config.JsonProvider
import com.projectronin.product.common.exception.BadRequestException
import com.projectronin.product.common.exception.ForbiddenException
import com.projectronin.product.common.exception.NotFoundException
import com.projectronin.product.common.exception.UnauthorizedException
import com.projectronin.product.common.exception.response.api.ErrorResponse
import io.mockk.clearAllMocks
import io.mockk.every
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.server.UnsupportedMediaTypeStatusException
import java.util.UUID
import kotlin.random.Random
import kotlin.reflect.KClass

private const val TEST_PATH = "/api/test"
private const val TEST_CUSTOM_VALIDATION_PATH = "/api/testCustomValidation"

@Import(TestConfigurationReference::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractCustomErrorHandlerIntegrationTest(
    val webTestClient: WebTestClient
) {
    companion object {
        private val objectMapper = JsonProvider.objectMapper

        private const val DEFAULT_AUTH_VALUE = "Bearer my_token"
        private val DEFAULT_AUTH_RESPONSE = AuthResponse(
            User(
                id = "userId123",
                tenantId = "tenantId456",
                name = Name("John", "John Doe", "Doe")
            ),
            UserSession()
        )

        private val DEFAULT_TEST_RESPONSE = TestResponse("userId123-tenantId456")
        private val DEFAULT_TEST_BODY = TestBody(UUID.randomUUID().toString(), Random.nextInt(10))
        private val DEFAULT_ID = DEFAULT_TEST_RESPONSE.id

        // grab test data for 'post with invalid body' scenarios
        @JvmStatic
        fun getInvalidBodyCases(): List<Arguments> {
            return TestCaseScenarios.getInvalidBodyCases()
        }

        @JvmStatic
        private fun customTypedExceptions(): Iterable<Exception> {
            return listOf(
                NotFoundException("not_found_exception"),
                UnauthorizedException("unauthorized_exception"),
                BadRequestException("bad_request_exception"),
                ForbiddenException("forbidden_exception")
            )
        }
    }

    @MockkBean
    lateinit var testService: TestEndpointService

    @MockkBean
    lateinit var sekiClient: SekiClient

    abstract val expectDetails: Boolean
    abstract val expectExceptionNames: Boolean
    abstract val expectStacktraces: Boolean

    @BeforeEach
    fun setup() {
        clearAllMocks() // must ensure mocks in clean state at beginning of each test.
    }

    @Test
    fun `test a simple valid request`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        webTestClient
            .post()
            .uri(TEST_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .bodyValue(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectHeader().location("$TEST_PATH/$DEFAULT_ID")
            .expectBody().json("""{"id":  "$DEFAULT_ID"}""")
    }

    @Test
    fun `test with no auth header at all`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        val response = webTestClient
            .post()
            .uri(TEST_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!
        checkResponse(
            response,
            "test with no auth header at all",
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            PreAuthenticatedCredentialsNotFoundException::class,
            "Authentication Error",
            "Token value was missing or invalid"
        )
    }

    @Test
    fun `test with auth exception`() {
        every { sekiClient.validate(any()) } throws RuntimeException("!")
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        val response = webTestClient
            .post()
            .uri(TEST_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .bodyValue(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!
        checkResponse(
            response,
            "test with auth exception",
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            AuthenticationServiceException::class,
            "Authentication Error",
            "Unable to verify seki token: !"
        )
    }

    @Test
    fun `test with auth bad credentials exception`() {
        every { sekiClient.validate(any()) } throws SekiInvalidTokenException("!")
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        val response = webTestClient
            .post()
            .uri(TEST_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .bodyValue(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!
        checkResponse(
            response,
            "test with auth bad credentials exception",
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            BadCredentialsException::class,
            "Authentication Error",
            "Invalid Seki Token"
        )
    }

    @Test
    fun `test a simple server error`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } throws RuntimeException("Unable to do the thing")

        val response = webTestClient
            .post()
            .uri(TEST_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .bodyValue(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
            .exchange()
            .expectStatus().is5xxServerError
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!
        checkResponse(
            response,
            "test a simple server error",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            RuntimeException::class,
            "Internal Server Error",
            "Unable to do the thing"
        )
    }

    @Test
    fun `test a argument type mismatch`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        val response = webTestClient
            .get()
            .uri("/api/testIntGetter/notaninteger")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

        checkResponse(
            response,
            "test a argument type mismatch",
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            ServerWebInputException::class,
            "Invalid value for field 'id'",
            "Type mismatch."
        )
    }

    @Test
    fun `test a typed error`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } throws NotFoundException("idvalue")

        val response = webTestClient
            .post()
            .uri(TEST_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .bodyValue(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

        checkResponse(
            response,
            "test a typed error",
            HttpStatus.NOT_FOUND,
            "Not Found",
            NotFoundException::class,
            "Not Found",
            "Item was not found: idvalue"
        )
    }

    @ParameterizedTest(name = "Exception \"{0}\" renders correct error code ")
    @MethodSource("customTypedExceptions")
    fun `test special typed exception response codes `(exception: Exception) {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } throws exception

        val expectedErrorStatus: HttpStatus? = AnnotationUtils.findAnnotation(exception.javaClass, ResponseStatus::class.java)?.code
        val response = webTestClient
            .post()
            .uri(TEST_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .bodyValue(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
            .exchange()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

        checkResponse(
            response,
            "test a typed error",
            expectedErrorStatus!!,
            expectedErrorStatus.reasonPhrase,
            exception::class,
            expectedErrorStatus.reasonPhrase,
            exception.message.orEmpty()
        )
    }

    @Test
    fun `test a preexisting response code`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        val response = webTestClient
            .post()
            .uri(TEST_PATH)
            .contentType(MediaType.APPLICATION_RSS_XML)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .bodyValue(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

        checkResponse(
            response,
            "test a preexisting response code",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Unsupported Media Type",
            UnsupportedMediaTypeStatusException::class,
            "Unsupported Media Type",
            """415 UNSUPPORTED_MEDIA_TYPE "Content type 'application/rss+xml' not supported for bodyType=com.projectronin.product.common.webfluxtest.TestBody""""
        )
    }

    @Test
    fun `test an annotated error`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } throws FooException()

        val response = webTestClient
            .post()
            .uri(TEST_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .bodyValue(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

        checkResponse(
            response,
            "test an annotated error`",
            HttpStatus.BANDWIDTH_LIMIT_EXCEEDED,
            "Bandwidth Limit Exceeded",
            FooException::class,
            "Bandwidth Limit Exceeded",
            "invalid something or other"
        )
    }

    /**
     * Test permutations of "bad post bodies".
     *    (note: different scenarios can tend to go down different code paths)
     */
    @ParameterizedTest(name = "Invalid Request Body: \"{0}\"")
    @MethodSource("getInvalidBodyCases")
    fun `invalid request body`(caseLabel: String, badCase: InvalidBodyBodyCase<Throwable>) {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        val response = webTestClient
            .post()
            .uri(badCase.endpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
            .bodyValue(badCase.body)
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ErrorResponse::class.java).returnResult().responseBody!!

        checkResponse(
            response,
            "invalid request body: $caseLabel",
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            badCase.expectedException,
            badCase.expectedMessage,
            badCase.expectedDetail
        )
    }

    private fun <T : Throwable> checkResponse(
        errorResponse: ErrorResponse,
        caseLabel: String,
        httpStatus: HttpStatus,
        error: String,
        exceptionClass: KClass<T>,
        message: String,
        detail: String
    ) {
        val extraInfo = "\n(checking '${objectMapper.writeValueAsString(errorResponse)}' for test case '$caseLabel' and failed validation)"

        fun supplyMessage(field: String, expected: Any, received: Any?): String =
            "Field $field [expected $expected but got $received]$extraInfo"

        assertThat(errorResponse.id)
            .withFailMessage { supplyMessage("id", "must look like a uuid", errorResponse.id) }
            .matches("""^[a-fA-F0-9-]{36}$""")
        assertThat(errorResponse.timestamp)
            .withFailMessage { supplyMessage("timestamp", "not null", errorResponse.timestamp) }
            .isNotNull
        assertThat(errorResponse.status)
            .withFailMessage { supplyMessage("status", httpStatus.value(), errorResponse.status) }
            .isEqualTo(httpStatus.value())
        assertThat(errorResponse.error)
            .withFailMessage { supplyMessage("error", error, errorResponse.error) }
            .isEqualTo(error)
        assertThat(errorResponse.exception)
            .withFailMessage { supplyMessage("exception", exceptionClass.java.name, errorResponse.exception) }
            .isEqualTo(if (expectExceptionNames) exceptionClass.java.name else "Exception")
        assertThat(errorResponse.message)
            .withFailMessage { supplyMessage("message", message, errorResponse.message) }
            .isEqualTo(message)
        assertThat(errorResponse.detail)
            .withFailMessage { supplyMessage("detail", detail, errorResponse.detail) }
            .isEqualTo(if (expectDetails) detail else null)
        if (expectStacktraces) {
            assertThat(errorResponse.stacktrace)
                .withFailMessage { supplyMessage("stacktrace", "not null", errorResponse.stacktrace) }
                .isNotNull
        } else {
            assertThat(errorResponse.stacktrace)
                .withFailMessage { supplyMessage("stacktrace", "null", errorResponse.stacktrace) }
                .isNull()
        }
    }
}

data class InvalidBodyBodyCase<T : Throwable>(
    val body: String,
    val expectedException: KClass<T>,
    val expectedMessage: String,
    val expectedDetail: String,
    val endpoint: String = TEST_PATH
)

private object TestCaseScenarios {
    /**
     * Grab test data inputs for "invalid POST body" test cases
     * ( note the first string param is only used as a label to make testcase names 'nicer' )
     */
    fun getInvalidBodyCases(): List<Arguments> {
        return listOf(
            Arguments.of(
                "invalid json", // json not parsable
                InvalidBodyBodyCase(
                    """{"foo": [is not json]}""",
                    JsonParseException::class,
                    "JSON Parse Error",
                    "Unrecognized token 'is': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')"
                )
            ),
            Arguments.of(
                "invalid format mapping", // pass in string for an 'int' field
                InvalidBodyBodyCase(
                    """{"value1": "foo", "value2": "bar"}""",
                    InvalidFormatException::class,
                    "Invalid value for field 'value2'",
                    "Cannot deserialize value of type `int` from String \"bar\": not a valid `int` value"
                )
            ),
            Arguments.of(
                "method argument validation - blank", // fails annotation check on field  @NotBlank / @NotEmpty
                InvalidBodyBodyCase(
                    """{"value1": "", "value2": 4}""",
                    WebExchangeBindException::class,
                    "Validation failure",
                    "[Field error in object 'testBody' on field 'value1': rejected value []; codes [NotBlank.testBody.value1,NotBlank.value1,NotBlank.java.lang.String,NotBlank]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [testBody.value1,value1]; arguments []; default message [value1]]; default message [must not be blank]]"
                )
            ),
            Arguments.of(
                "method argument validation - invalid", // fails annotation check on field  (like @size or @max)
                InvalidBodyBodyCase(
                    """{"value1": "foo", "value2": 30000}""",
                    WebExchangeBindException::class,
                    "Validation failure",
                    "[Field error in object 'testBody' on field 'value2': rejected value [30000]; codes [Max.testBody.value2,Max.value2,Max.int,Max]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [testBody.value2,value2]; arguments []; default message [value2],10]; default message [must be less than or equal to 10]]"
                )
            ),
            Arguments.of(
                "constraint validation", // different error path taken on endpoint w/o '@Valid' annotation
                InvalidBodyBodyCase(
                    """{"value1": "foo", "value2": 20000}""",
                    ConstraintViolationException::class,
                    "Invalid value for field 'value2'",
                    "must be less than or equal to 10",
                    TEST_CUSTOM_VALIDATION_PATH
                )
            ),
            Arguments.of(
                "missing value on non-nullable field", // field missing from payload on non-nullable field
                InvalidBodyBodyCase(
                    """{"value2": 4}""",
                    MissingKotlinParameterException::class,
                    "Missing required field 'value1'",
                    "Instantiation of [simple type, class com.projectronin.product.common.webfluxtest.TestBody] value failed for JSON property value1 due to missing (therefore NULL) value for creator parameter value1 which is a non-nullable type"
                )
            ),
            Arguments.of(
                "null value on non-nullable field", // passing in explicit null on non-nullable field
                InvalidBodyBodyCase(
                    """{"value1": null, "value2": 4}""",
                    MissingKotlinParameterException::class,
                    "Missing required field 'value1'",
                    "Instantiation of [simple type, class com.projectronin.product.common.webfluxtest.TestBody] value failed for JSON property value1 due to missing (therefore NULL) value for creator parameter value1 which is a non-nullable type"
                )
            )
        )
    }
}
