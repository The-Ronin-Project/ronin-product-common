package com.projectronin.product.common.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.product.common.client.auth.AuthBroker
import com.projectronin.product.common.client.exception.ServiceClientException
import com.projectronin.product.common.client.exception.ServiceClientExceptionHandler
import com.projectronin.product.common.config.JsonProvider
import mu.KotlinLogging
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

private val MEDIA_TYPE_APPLICATION_JSON: okhttp3.MediaType = MediaType.APPLICATION_JSON_VALUE.toMediaType()

/**
 * Base Client class for handling common logic for all Service Rest Clients
 */
abstract class AbstractServiceClient(
    hostUrl: String,
    protected val authBroker: AuthBroker,
    protected val client: OkHttpClient = defaultOkHttpClient(),
    protected val objectMapper: ObjectMapper = defaultMapper(),
    protected val exceptionHandler: ServiceClientExceptionHandler = defaultExceptionHandler(objectMapper)
) {
    protected val logger = KotlinLogging.logger { }
    val baseUrl = if (hostUrl.endsWith("/")) hostUrl else "$hostUrl/"

    // TODO - still TBD if really want this as an abstract method
    protected abstract fun getUserAgentValue(): String

    /**
     * Convenience method that does a GET call and converts the response into the class type given
     * @param requestUrl requestUrl
     * @return the responseBody converted into an object of the given type
     * @throws ServiceClientException exception thrown for any error (including http 4xx and 5xx status codes)
     */
    @Throws(ServiceClientException::class)
    protected inline fun <reified T> executeGet(requestUrl: String): T {
        val serviceResponse = executeRawGet(requestUrl)
        return convertStringToObject(serviceResponse.body)
    }

    /**
     * Convenience method that does a POST call and converts the response into the class type given
     * @param requestUrl requestUrl
     * @param requestPayload object be sent on the request as payload body
     * @return the responseBody converted into an object of the given type
     * @throws ServiceClientException exception thrown for any error (including http 4xx and 5xx status codes)
     */
    @Throws(ServiceClientException::class)
    protected inline fun <reified T> executePost(requestUrl: String, requestPayload: Any): T {
        val serviceResponse = executeRawPost(requestUrl, requestPayload)
        return convertStringToObject(serviceResponse.body)
    }

    @Throws(ServiceClientException::class)
    protected open fun executeRawGet(requestUrl: String, shouldTrhowOnStatusError: Boolean = DEFAULT_THROW_ON_HTTP_ERROR): ServiceResponse {
        val request = generateRequest("GET", requestUrl)
        return executeRequest(request, shouldTrhowOnStatusError)
    }

    @Throws(ServiceClientException::class)
    protected open fun executeRawPost(requestUrl: String, requestPayload: Any, shouldTrhowOnStatusError: Boolean = DEFAULT_THROW_ON_HTTP_ERROR): ServiceResponse {
        val request = generateRequest("POST", requestUrl, requestPayload)
        return executeRequest(request, shouldTrhowOnStatusError)
    }

    @Throws(ServiceClientException::class)
    protected open fun executeRawDelete(requestUrl: String, shouldTrhowOnStatusError: Boolean = DEFAULT_THROW_ON_HTTP_ERROR): ServiceResponse {
        val request = generateRequest("DELETE", requestUrl)
        return executeRequest(request, shouldTrhowOnStatusError)
    }

    /**
     * Deserialize a JSON string into an object
     * @param jsonString input JSON
     * @return object constructed from the json string
     */
    protected inline fun <reified T> convertStringToObject(jsonString: String): T {
        return when (T::class) {
            String::class -> jsonString as T // if asked to convert from String -to-> String, just return the input
            else -> objectMapper.readValue<T>(jsonString)
        }
    }

    /**
     * Serialize an object into a JSON string
     * @param inputObject the object to serialize
     * @return JSON string representation of inputObject
     */
    protected fun convertObjectToString(inputObject: Any): String {
        return when (inputObject) {
            is String -> inputObject // no conversion needed if input already a string
            else -> objectMapper.writeValueAsString(inputObject)
        }
    }

    /**
     * Executes request and returns a response
     * @param request the request to execute
     * @param shouldTrhowOnStatusError flag to indicate to throw exception on http error code 4xx and 5xx
     * @return response object
     * @throws ServiceClientException exception thrown for any error (underlying exception found in the nested 'cause')
     */
    @Throws(ServiceClientException::class)
    protected open fun executeRequest(request: Request, shouldTrhowOnStatusError: Boolean = DEFAULT_THROW_ON_HTTP_ERROR): ServiceResponse {
        try {
            val rawResponse: Response = client.newCall(request).execute()
            val serviceResponse = buildServiceResponse(rawResponse)
            if (serviceResponse.httpStatus.isError && shouldTrhowOnStatusError) {
                exceptionHandler.handleError(serviceResponse)
            }
            return serviceResponse
        } catch (e: Exception) {
            exceptionHandler.handleException(e)
        }
    }

    /**
     * Create request to be executed
     * @param method the request method type ("GET", "POST", etc)
     * @param requesturl the request url
     * @param requestPayload object for request payload body (null if not applicable)
     * @return request object
     */
    protected open fun generateRequest(method: String, requesturl: String, requestPayload: Any? = null): Request {
        return Request.Builder()
            .method(method, generateRequestBody(requestPayload))
            .url(requesturl)
            .headers(generateRequestHeaders(method, requesturl, authBroker.authToken))
            .build()
    }

    /**
     * Convert object into a requestBody
     * @param requestPayload object representing the request body
     * @return requestBody (or null)
     */
    protected open fun generateRequestBody(requestPayload: Any?): RequestBody? {
        return if (requestPayload != null) {
            convertObjectToString(requestPayload).toRequestBody(MEDIA_TYPE_APPLICATION_JSON)
        } else {
            null
        }
    }

    /**
     * Create Headers object for request
     * @param bearerAuthToken token to be used for Authorization header (use "" for no auth header)
     * @param requestUrl requestUrl used to determine which the request Headers to use
     * @return Headers
     */
    private fun generateRequestHeaders(method: String, requestUrl: String, bearerAuthToken: String): Headers {
        return Headers.Builder().apply {
            for (entry in getRequestHeaderMap(method, requestUrl, bearerAuthToken)) {
                add(entry.key, entry.value)
            }
        }.build()
    }

    /**
     * Generates a request header key/value map of headers to be used on the request
     *   (this method can be overridden as necessary for customizable request headers)
     * @param bearerAuthToken token to be used for Authorization header (use "" for no auth header)
     * @param method the request method type ("GET", "POST", etc)
     * @param requestUrl  requestUrl (optionally) used to determine which the request Headers to use
     * @return Map<String,String> of headers to be used for the request
     */
    // TODO - need to confirm that okHttpClient already auto-magically
    //   includes request headers like: "Accept-Encoding: gzip"
    //   i'm pretty sure it does but shouldn't assume....  b/c Elixir sure doesn't !!!  :-p
    protected open fun getRequestHeaderMap(method: String = "", requestUrl: String = "", bearerAuthToken: String = ""): MutableMap<String, String> {
        return mutableMapOf(
            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.USER_AGENT to getUserAgentValue()
        ).apply {
            if (bearerAuthToken.isNotEmpty()) {
                put(HttpHeaders.AUTHORIZATION, "Bearer $bearerAuthToken")
            }
        }
    }

    /**
     * Used to create a default parameters if not supplied on the constructor
     */
    companion object {
        @JvmStatic
        protected fun defaultMapper(): ObjectMapper {
            return JsonProvider.objectMapper
        }

        @JvmStatic
        protected fun defaultExceptionHandler(objectMapper: ObjectMapper): ServiceClientExceptionHandler {
            return ServiceClientExceptionHandler(objectMapper)
        }

        protected const val DEFAULT_THROW_ON_HTTP_ERROR = true

        @JvmStatic
        protected fun defaultOkHttpClient(): OkHttpClient {
            return StdHttpClientFactory.createClient()
        }
    }

    /**
     * Convert the original okHttp Response to more generic response
     *  then call 'close' on the okHttp Response
     */
    protected open fun buildServiceResponse(rawResponse: Response): ServiceResponse {
        rawResponse.use { // 'use' will call close() on the rawResponse
            return ServiceResponse(
                httpCode = rawResponse.code,
                body = rawResponse.body?.string() ?: "",
                headerMap = rawResponse.headers.toMap()
            )
        }
    }
}
