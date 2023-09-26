package com.projectronin.product.common.auth

import com.projectronin.auth.RoninAuthentication
import com.projectronin.product.common.auth.seki.client.SekiClient
import com.projectronin.product.common.auth.seki.client.exception.SekiClientException
import com.projectronin.product.common.auth.seki.client.exception.SekiInvalidTokenException
import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.auth.seki.client.model.Name
import com.projectronin.product.common.auth.seki.client.model.User
import com.projectronin.product.common.auth.seki.client.model.UserSession
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpHeaders
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler

private const val EXPECTED_MISSING_TOKEN_ERR_MSG = "Token value was missing or invalid"
private const val EXPECTED_INVALID_TOKEN_ERR_MSG = EXPECTED_MISSING_TOKEN_ERR_MSG

class SekiAuthTokenHeaderFilterTest {

    @RelaxedMockK
    private lateinit var mockSekiClient: SekiClient

    @RelaxedMockK
    private lateinit var mockRequest: HttpServletRequest

    @RelaxedMockK
    private lateinit var mockResponse: HttpServletResponse

    @RelaxedMockK
    private lateinit var mockChain: FilterChain

    private var request: HttpServletRequest? = null
    private var response: HttpServletResponse? = null
    private var exception: AuthenticationException? = null

    private val testErrorHandler = AuthenticationFailureHandler { request, response, exception ->
        this.request = request
        this.response = response
        this.exception = exception
    }

    @BeforeEach
    fun setup() {
        SecurityContextHolder.clearContext()
        clearAllMocks() // must ensure mocks in clean state at beginning of each test.
        MockKAnnotations.init(this)
        this.request = null
        this.response = null
        this.exception = null
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Nested
    @DisplayName("Get Token from Auth Header")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetToken {

        @Test
        fun `valid token from auth header and call seki`() {
            val testToken = "123abc"
            val dummyAuthResponse = getDummyAuthResponse()
            val dummyUser = dummyAuthResponse.user

            // add Authorization header to the mockRequest
            every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer $testToken"
            // return dummy authResponse when the mock seki client 'validate' is called.
            every { mockSekiClient.validate(testToken) } returns dummyAuthResponse

            SekiAuthTokenHeaderFilter(mockSekiClient, testErrorHandler).doFilter(mockRequest, mockResponse, mockChain)

            // ensure sekiClient was called with our token
            verify(exactly = 1) { mockSekiClient.validate(testToken) }
            // ensure doFilter was called to continue on with the request.
            verify(exactly = 1) { mockChain.doFilter(mockRequest, mockResponse) }

            // check that the context now has the expected authorization instance
            //    NOTE:  seems like there should be a 'nicer' way to access this
            val context = SecurityContextHolder.getContext()
            assertThat(context.authentication).isInstanceOf(RoninAuthentication::class.java)
            val roninAuth = context.authentication as RoninAuthentication
            Assertions.assertEquals(dummyUser.id, roninAuth.userId, "mismatch expected userId")
            Assertions.assertEquals(dummyUser.firstName, roninAuth.userFirstName, "mismatch expected userFirstName")
            Assertions.assertEquals(dummyUser.lastName, roninAuth.userLastName, "mismatch expected userLastName")
            Assertions.assertEquals(dummyUser.fullName, roninAuth.userFullName, "mismatch expected userFullName")
            Assertions.assertEquals(dummyUser.tenantId, roninAuth.tenantId, "mismatch expected tenantId")
            Assertions.assertEquals(dummyUser.udpId, roninAuth.udpId, "mismatch expected udpId")
            Assertions.assertEquals(roninAuth.tokenValue, testToken, "mismatch expected $testToken")
        }

        @Test
        fun `handling seki validate exception`() {
            val testToken = "123abc"

            // add Authorization header to the mockRequest
            every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer $testToken"
            // make a call to sekiClient.validate throw an exception
            every { mockSekiClient.validate(testToken) } throws SekiInvalidTokenException("bad token!")

            val exception = assertThrows<AuthenticationException> {
                SekiAuthTokenHeaderFilter(mockSekiClient, testErrorHandler).attemptAuthentication(
                    mockRequest,
                    mockResponse
                )
            }
            Assertions.assertEquals("Invalid Seki Token", exception.message)
        }

        @Test
        fun `handling seki internal exception`() {
            val testToken = "123abc"
            val exceptionMessage = "Seki Exception occurred!!"

            // add Authorization header to the mockRequest
            every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns "Bearer $testToken"
            // make a call to sekiClient.validate throw an exception
            every { mockSekiClient.validate(testToken) } throws SekiClientException(exceptionMessage)

            val exception = assertThrows<AuthenticationException> {
                SekiAuthTokenHeaderFilter(mockSekiClient, testErrorHandler).attemptAuthentication(
                    mockRequest,
                    mockResponse
                )
            }
            Assertions.assertEquals("Unable to verify seki token: $exceptionMessage", exception.message)
        }

        @ParameterizedTest(name = "test missing token in auth header value \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = ["Bearer", "Bearer ", "Bearer      "])
        fun `missing token in auth header`(authHeaderValue: String?) {
            // add Authorization header without a token to the mock
            every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns authHeaderValue

            val exception = assertThrows<AuthenticationException> {
                SekiAuthTokenHeaderFilter(mockSekiClient, testErrorHandler).attemptAuthentication(
                    mockRequest,
                    mockResponse
                )
            }
            Assertions.assertEquals(com.projectronin.product.common.auth.EXPECTED_MISSING_TOKEN_ERR_MSG, exception.message)
            verify(exactly = 0) { mockSekiClient.validate(any()) } // verify did NOT attempt to call seki b/c no token available
        }

        @Test
        fun `invalid auth header`() {
            // give mockRequest an invalid/unsupported value for Authorization header
            every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns "Basic some_token_value"

            val exception = assertThrows<AuthenticationException> {
                SekiAuthTokenHeaderFilter(mockSekiClient, testErrorHandler).attemptAuthentication(
                    mockRequest,
                    mockResponse
                )
            }
            Assertions.assertEquals(com.projectronin.product.common.auth.EXPECTED_INVALID_TOKEN_ERR_MSG, exception.message)
            verify(exactly = 0) { mockSekiClient.validate(any()) } // verify did NOT attempt to call seki b/c invalid token
        }
    }

    @Nested
    @DisplayName("Get Token from Cookie")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetCookie {

        @Test
        fun `valid token from auth header and call seki`() {
            val testToken = "123abc"
            val dummyState = "dummy-state-123"
            val dummyAuthResponse = getDummyAuthResponse()
            val dummyUser = dummyAuthResponse.user

            // add Authorization header to the mockRequest
            every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns null
            every { mockRequest.getHeader(COOKIE_STATE_HEADER) } returns dummyState
            every { mockRequest.cookies } returns arrayOf(
                mockk {
                    every { name } returns "$COOKIE_STATE_NAME_PREFIX$dummyState"
                    every { value } returns testToken
                }
            )
            // return dummy authResponse when the mock seki client 'validate' is called.
            every { mockSekiClient.validate(testToken) } returns dummyAuthResponse

            SekiAuthTokenHeaderFilter(mockSekiClient, testErrorHandler).doFilter(mockRequest, mockResponse, mockChain)

            // ensure sekiClient was called with our token
            verify(exactly = 1) { mockSekiClient.validate(testToken) }
            // ensure doFilter was called to continue on with the request.
            verify(exactly = 1) { mockChain.doFilter(mockRequest, mockResponse) }

            // check that the context now has the expected authorization instance
            //    NOTE:  seems like there should be a 'nicer' way to access this
            val context = SecurityContextHolder.getContext()
            assertThat(context.authentication).isInstanceOf(RoninAuthentication::class.java)
            val roninAuth = context.authentication as RoninAuthentication
            Assertions.assertEquals(dummyUser.id, roninAuth.userId, "mismatch expected userId")
            Assertions.assertEquals(dummyUser.firstName, roninAuth.userFirstName, "mismatch expected userFirstName")
            Assertions.assertEquals(dummyUser.lastName, roninAuth.userLastName, "mismatch expected userLastName")
            Assertions.assertEquals(dummyUser.fullName, roninAuth.userFullName, "mismatch expected userFullName")
            Assertions.assertEquals(dummyUser.tenantId, roninAuth.tenantId, "mismatch expected tenantId")
            Assertions.assertEquals(dummyUser.udpId, roninAuth.udpId, "mismatch expected udpId")
        }

        // UnitTest to confirm fix for bug DASH-3918
        @Test
        fun `handling state header without cookies`() {
            val dummyState = "dummy-state-123"

            // add Authorization header to the mockRequest
            every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns null
            every { mockRequest.getHeader(COOKIE_STATE_HEADER) } returns dummyState
            every { mockRequest.cookies } returns null // cookies explicitly absent

            val exception = assertThrows<AuthenticationException> {
                SekiAuthTokenHeaderFilter(mockSekiClient, testErrorHandler).attemptAuthentication(
                    mockRequest,
                    mockResponse
                )
            }
            Assertions.assertEquals(com.projectronin.product.common.auth.EXPECTED_MISSING_TOKEN_ERR_MSG, exception.message)
            verify(exactly = 0) { mockSekiClient.validate(any()) } // verify did NOT attempt to call seki b/c no token available
        }

        @Test
        fun `handling seki validate exception`() {
            // This test has all its own mocks but still doesn't work when run with other tests
            val response: HttpServletResponse = mockk(relaxed = true)
            val sekiClient: SekiClient = mockk()
            val testToken = "123abc"
            val dummyState = "dummy-state-123"
            val request: HttpServletRequest = mockk(relaxed = true) {
                every { getHeader(HttpHeaders.AUTHORIZATION) } returns null
                every { getHeader(COOKIE_STATE_HEADER) } returns dummyState
                every { cookies } returns arrayOf(
                    mockk {
                        every { name } returns "$COOKIE_STATE_NAME_PREFIX$dummyState"
                        every { value } returns testToken
                    }
                )
            }

            // add Authorization header to the mockRequest
            // make a call to sekiClient.validate throw an exception
            every { sekiClient.validate(testToken) } throws SekiInvalidTokenException("bad token!")

            val exception = assertThrows<AuthenticationException> {
                SekiAuthTokenHeaderFilter(sekiClient, testErrorHandler).attemptAuthentication(
                    request,
                    response
                )
            }
            Assertions.assertEquals("Invalid Seki Token", exception.message)
        }

        @Test
        fun `handling seki internal exception`() {
            val testToken = "123abc"
            val dummyState = "dummy-state-123"
            val exceptionMessage = "Seki Exception occurred!!"

            // add Authorization header to the mockRequest
            every { mockRequest.getHeader(HttpHeaders.AUTHORIZATION) } returns null
            every { mockRequest.getHeader(COOKIE_STATE_HEADER) } returns dummyState
            every { mockRequest.cookies } returns arrayOf(
                mockk {
                    every { name } returns "$COOKIE_STATE_NAME_PREFIX$dummyState"
                    every { value } returns testToken
                }
            )
            // make a call to sekiClient.validate throw an exception
            every { mockSekiClient.validate(testToken) } throws SekiClientException(exceptionMessage)

            val exception = assertThrows<AuthenticationException> {
                SekiAuthTokenHeaderFilter(mockSekiClient, testErrorHandler).attemptAuthentication(
                    mockRequest,
                    mockResponse
                )
            }
            Assertions.assertEquals("Unable to verify seki token: $exceptionMessage", exception.message)
        }
    }

    private fun getDummyAuthResponse(): AuthResponse {
        return AuthResponse(
            User(
                id = "userId123",
                tenantId = "tenantId456",
                udpId = "some-long-string-398091830899-Z",
                name = Name("John", "John Doe", "Doe")
            ),
            UserSession()
        )
    }
}
