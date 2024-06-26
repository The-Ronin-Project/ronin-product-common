package com.projectronin.product.common.exception

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
import com.projectronin.product.common.exception.response.api.ErrorResponse
import com.projectronin.product.common.test.FooException
import com.projectronin.product.common.test.TestBody
import com.projectronin.product.common.test.TestConfigurationReference
import com.projectronin.product.common.test.TestEndpointService
import com.projectronin.product.common.test.TestResponse
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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.UUID
import kotlin.random.Random
import kotlin.reflect.KClass

private const val TEST_PATH = "/api/test"
private const val TEST_CUSTOM_VALIDATION_PATH = "/api/testCustomValidation"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = [TestConfigurationReference::class])
abstract class AbstractCustomErrorHandlerIntegrationTest(
    val mockMvc: MockMvc
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

        private val DEFAULT_TEST_RESPONSE = TestResponse(UUID.randomUUID().toString())
        private val DEFAULT_TEST_BODY = TestBody(UUID.randomUUID().toString(), Random.nextInt(10))
        private val DEFAULT_ID = DEFAULT_TEST_RESPONSE.id

        // grab test data for 'post with invalid body' scenarios
        @JvmStatic
        fun getInvalidBodyCases(): List<Arguments> {
            return TestCaseScenarios.getInvalidBodyCases()
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

        // NOTE: the 'Date' response header is (currently) not available here,
        //   but _IS_ available when the app is running.
        mockMvc.perform(
            MockMvcRequestBuilders.post(TEST_PATH)
                .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("\$.id").value(DEFAULT_ID))
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andExpect(MockMvcResultMatchers.header().string("Location", "http://localhost$TEST_PATH/$DEFAULT_ID"))
            .andReturn()
    }

    @Test
    fun `test with no auth header at all`() {
        // when auth toekn is missing, expect a response with a 401 status code.
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        mockMvc.perform(
            MockMvcRequestBuilders.post(TEST_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()
    }

    @Test
    fun `test with auth exception`() {
        // if an exception occurred during the authorization process, it will be treated
        //  the same as if the user is 'unauthorized', and will return a 401 error response.
        every { sekiClient.validate(any()) } throws RuntimeException("!")
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        mockMvc.perform(
            MockMvcRequestBuilders.post(TEST_PATH)
                .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()
    }

    @Test
    fun `test with auth bad credentials exception`() {
        // expect request with bad creds to reutnr a 401 error response
        every { sekiClient.validate(any()) } throws SekiInvalidTokenException("!")
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        mockMvc.perform(
            MockMvcRequestBuilders.post(TEST_PATH)
                .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()
    }

    @Test
    fun `test a simple server error`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } throws RuntimeException("Unable to do the thing")

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post(TEST_PATH)
                .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
        )
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, ErrorResponse::class.java)

        assertThat(response).isNotNull
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

    /**
     * Test permutations of "bad post bodies".
     *    (note: different scenarios can tend to go down different code paths)
     */
    @ParameterizedTest(name = "Invalid Request Body: \"{0}\"")
    @MethodSource("getInvalidBodyCases")
    fun `invalid request body`(caseLabel: String, badCase: InvalidBodyBodyCase<Throwable>) {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post(badCase.endpoint)
                .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(badCase.body)
        )
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.BAD_REQUEST.value()))
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, ErrorResponse::class.java)
        assertThat(response).isNotNull
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

    @Test
    fun `test a argument type mismatch`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/testIntGetter/notaninteger")
                .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.BAD_REQUEST.value()))
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, ErrorResponse::class.java)

        assertThat(response).isNotNull
        checkResponse(
            response,
            "test a argument type mismatch",
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            MethodArgumentTypeMismatchException::class,
            "Invalid value for field 'id'",
            "Failed to convert value of type 'java.lang.String' to required type 'int'; For input string: \"notaninteger\""
        )
    }

    @Test
    fun `test a typed error`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } throws NotFoundException("idvalue")

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post(TEST_PATH)
                .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
        )
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, ErrorResponse::class.java)

        assertThat(response).isNotNull
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

    @Test
    fun `test a preexisting response code`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } returns DEFAULT_TEST_RESPONSE

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post(TEST_PATH)
                .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
                .contentType(MediaType.APPLICATION_RSS_XML)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
        )
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, ErrorResponse::class.java)

        assertThat(response).isNotNull
        checkResponse(
            response,
            "test a preexisting response code",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Unsupported Media Type",
            HttpMediaTypeNotSupportedException::class,
            "Unsupported Media Type",
            "Content-Type 'application/rss+xml;charset=UTF-8' is not supported"
        )
    }

    @Test
    fun `test an annotated error`() {
        every { sekiClient.validate(any()) } returns DEFAULT_AUTH_RESPONSE
        every { testService.getTestResponse() } throws FooException()

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post(TEST_PATH)
                .header(HttpHeaders.AUTHORIZATION, DEFAULT_AUTH_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DEFAULT_TEST_BODY))
        )
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.value()))
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json"))
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, ErrorResponse::class.java)

        assertThat(response).isNotNull
        checkResponse(
            response,
            "test an annotated error",
            HttpStatus.BANDWIDTH_LIMIT_EXCEEDED,
            "Bandwidth Limit Exceeded",
            FooException::class,
            "Bandwidth Limit Exceeded",
            "invalid something or other"
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
                    MethodArgumentNotValidException::class,
                    "Missing required field 'value1'",
                    "must not be blank"
                )
            ),
            Arguments.of(
                "method argument validation - invalid", // fails annotation check on field  (like @size or @max)
                InvalidBodyBodyCase(
                    """{"value1": "foo", "value2": 30000}""",
                    MethodArgumentNotValidException::class,
                    "Invalid value for field 'value2'",
                    "must be less than or equal to 10"
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
                    "Instantiation of [simple type, class com.projectronin.product.common.test.TestBody] value failed for JSON property value1 due to missing (therefore NULL) value for creator parameter value1 which is a non-nullable type"
                )
            ),
            Arguments.of(
                "null value on non-nullable field", // passing in explicit null on non-nullable field
                InvalidBodyBodyCase(
                    """{"value1": null, "value2": 4}""",
                    MissingKotlinParameterException::class,
                    "Missing required field 'value1'",
                    "Instantiation of [simple type, class com.projectronin.product.common.test.TestBody] value failed for JSON property value1 due to missing (therefore NULL) value for creator parameter value1 which is a non-nullable type"
                )
            )
        )
    }
}
