package com.projectronin.product.common.test

import com.projectronin.product.common.config.JsonProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions
import org.springframework.http.HttpHeaders

object TestMockHttpClientFactory {

    /**
     * Create a mock HttpClient object (to be used for a single call)
     * @param responseCode httpStatusCode to be returned by the mockClient call
     * @param responseBody responseBody to be returned by mockClient (this will be converted into a String)
     * @param exceptedRequestValues [OPTIONAL]: extra params to validate the request 'looks' correct before returning mock response
     * @return Mocked version of OkHttpClient
     */
    fun createMockClient(
        responseCode: Int,
        responseObject: Any,
        expectedReqValues: ExceptedRequestValues? = null,
    ): OkHttpClient {
        val responseString = convertObjectToString(responseObject)
        val mockHttpResponse = mockk<Response>()
        val mockHttpResponseBody = mockk<ResponseBody>()
        val mockHttpClient = mockk<OkHttpClient>()

        val requestSlot = slot<Request>()
        // NOTE: 'answers' is basically like returns, except it's a lambda
        //   so you can do 'extra stuff' before actually returning the mocked response
        //     in this case validation on the request
        every {
            mockHttpClient.newCall(capture(requestSlot)).execute()
        } answers {
            validateExpectedRequest(requestSlot.captured, expectedReqValues) // run validation on request (as applicable)
            mockHttpResponse // this is the actual 'returns' value
        }

        every { mockHttpResponse.code } returns responseCode
        every { mockHttpResponse.body } returns mockHttpResponseBody
        every { mockHttpResponseBody.string() } returns responseString
        every { mockHttpResponse.close() } returns Unit
        every { mockHttpResponse.headers } returns Headers.headersOf(HttpHeaders.CONTENT_TYPE, "application/json")

        return mockHttpClient
    }

    /**
     * Create a mock HttpClient object that will throw an exception
     * @param exception exception to be thrown
     * @param exceptedRequestValues [OPTIONAL]: extra params to validate the request 'looks' correct before returning mock response
     * @return Mocked version of OkHttpClient
     */
    fun createMockClient(
        exception: Exception,
        expectedReqValues: ExceptedRequestValues? = null,
    ): OkHttpClient {
        val mockHttpClient = mockk<OkHttpClient>()

        val requestSlot = slot<Request>()
        // NOTE: 'answers' is basically like returns, except it's a lambda
        //   so you can do 'extra stuff' before actually returning the mocked response
        //     in this case validation on the request
        every {
            mockHttpClient.newCall(capture(requestSlot)).execute()
        } answers {
            validateExpectedRequest(requestSlot.captured, expectedReqValues) // run validation on request (as applicable)
            throw exception
        }
        return mockHttpClient
    }

    private fun validateExpectedRequest(request: Request, expectedReqValues: ExceptedRequestValues?) {
        if (expectedReqValues == null) {
            return
        }
        if (expectedReqValues.method != "") {
            Assertions.assertEquals(expectedReqValues.method, request.method, "mismatch expected request method")
        }
        if (expectedReqValues.requestUrl != "") {
            Assertions.assertEquals(
                expectedReqValues.requestUrl,
                request.url.toString(),
                "mismatch expected request url"
            )
        }
        if (expectedReqValues.headerMap.isNotEmpty()) {
            // note: only check that the request has all of the expected headers.
            //   we do NOT check if the request has any 'extra' request headers not specified in expected map.
            val requestMap = request.headers.toMap()
            for ((key, value) in expectedReqValues.headerMap) {
                val reqHeaderValue = requestMap.get(key)
                Assertions.assertEquals(
                    reqHeaderValue,
                    value,
                    "mismatch expected request header value for header '$key'"
                )
            }
        }
    }

    private fun convertObjectToString(inputObject: Any): String {
        return when (inputObject) {
            is String -> inputObject // no conversion needed if input already a string
            else -> JsonProvider.objectMapper.writeValueAsString(inputObject)
        }
    }
}
