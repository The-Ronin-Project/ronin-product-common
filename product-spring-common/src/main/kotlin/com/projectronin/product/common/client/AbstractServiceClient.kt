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
import java.net.HttpCookie
import javax.servlet.http.Cookie

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

    // TODO ******** - continuing to waffle on how want the GET/POST/DELETE calls to be *******  (in flux!)

    /**
     * Convenience method that does a GET call and converts the response into the class type given
     * @param requestUrl requestUrl
     * @return the responseBody converted into an object of the given type
     * @throws ServiceClientException exception thrown for any error (including http 4xx and 5xx status codes)
     */
    @Throws(ServiceClientException::class)
    protected inline fun <reified T> executeGet(requestUrl: String, extraHeaderMap: Map<String, String> = emptyMap()): T {
        val serviceResponse = executeRequest(GetRequest(url = requestUrl, extraHeaderMap = extraHeaderMap))
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
    protected inline fun <reified T> executePost(requestUrl: String, requestPayload: Any, extraHeaderMap: Map<String, String> = emptyMap()): T {
        val serviceResponse = executeRequest(PostRequest(url = requestUrl, payload = requestPayload, extraHeaderMap = extraHeaderMap))
        return convertStringToObject(serviceResponse.body)
    }

    @Throws(ServiceClientException::class)
    protected fun executeDelete(requestUrl: String, extraHeaderMap: Map<String, String> = emptyMap()): String {
        val serviceResponse = executeRequest(DeleteRequest(url = requestUrl, extraHeaderMap = extraHeaderMap))
        return serviceResponse.body
    }

    // todo: better class name
    open class BaseRequest(
        val method: String,
        val url: String,
        val payload: Any? = null,
        val extraHeaderMap: Map<String, String> = emptyMap(),
        val shouldThrowOnStatusError: Boolean = DEFAULT_THROW_ON_HTTP_ERROR
    )

    open class GetRequest(url: String, extraHeaderMap: Map<String, String> = emptyMap(), shouldThrowOnStatusError: Boolean = DEFAULT_THROW_ON_HTTP_ERROR) :
        BaseRequest("GET", url, null, extraHeaderMap, shouldThrowOnStatusError)

    open class PostRequest(url: String, payload: Any, extraHeaderMap: Map<String, String> = emptyMap(), shouldThrowOnStatusError: Boolean = DEFAULT_THROW_ON_HTTP_ERROR) :
        BaseRequest("POST", url, payload, extraHeaderMap, shouldThrowOnStatusError)

    open class DeleteRequest(url: String, extraHeaderMap: Map<String, String> = emptyMap(), shouldThrowOnStatusError: Boolean = DEFAULT_THROW_ON_HTTP_ERROR) :
        BaseRequest("DELETE", url, null, extraHeaderMap, shouldThrowOnStatusError)

    /**
     * Executes request and returns a response
     * @param request the request to execute
     * @return response object
     * @throws ServiceClientException exception thrown for any error (underlying exception found in the nested 'cause')
     */
    @Throws(ServiceClientException::class)
    protected open fun executeRequest(request: BaseRequest): ServiceResponse {
        return executeRequest(generateOkHttpRequest(request), request.shouldThrowOnStatusError)
    }

    /**
     * Executes request and returns a response
     * @param request the internal request to execute
     * @param shouldThrowOnStatusError flag to indicate to throw exception on http error code 4xx and 5xx
     * @return response object
     * @throws ServiceClientException exception thrown for any error (underlying exception found in the nested 'cause')
     */
    @Throws(ServiceClientException::class)
    protected open fun executeRequest(request: Request, shouldThrowOnStatusError: Boolean = DEFAULT_THROW_ON_HTTP_ERROR): ServiceResponse {
        try {
            val rawResponse: Response = client.newCall(request).execute()
            val serviceResponse = buildServiceResponse(rawResponse)
            if (serviceResponse.httpStatus.isError && shouldThrowOnStatusError) {
                exceptionHandler.handleError(serviceResponse)
            }
            return serviceResponse
        } catch (e: Exception) {
            exceptionHandler.handleException(e)
        }
    }

    /**
     * Converts a BaseRequest into an OkHttpReqeust to be executed.
     */
    protected fun generateOkHttpRequest(request: BaseRequest): Request {
        val reqHeaderMap =
            generateRequestHeaderMap(request.method, request.url, authBroker.authToken, request.extraHeaderMap)
        val headers = Headers.Builder().apply {
            for (entry in reqHeaderMap) {
                add(entry.key, entry.value)
            }
        }.build()

        val okHttpRequest = Request.Builder()
            .method(request.method, generateRequestBody(request.payload))
            .url(request.url)
            .headers(headers)
            .build()
        return okHttpRequest
    }

    /**
     * Deserialize a JSON string into an object
     * @param jsonString input JSON
     * @return object constructed from the json string
     */
    protected inline fun <reified T> convertStringToObject(jsonString: String): T {
        return when (T::class) {
            String::class -> jsonString as T // if asked to convert from String -to-> String, just return the input
            else -> {
                try {
                    objectMapper.readValue<T>(jsonString)
                } catch (e: Exception) {
                    throw ServiceClientException("Unable to deserialize string into object type '${T::class.simpleName}': ${e.message}", e)
                }
            }
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
            else -> {
                try {
                    objectMapper.writeValueAsString(inputObject)
                } catch (e: Exception) {
                    throw ServiceClientException("Unable to serialize object type '${inputObject.javaClass.simpleName}' into a JSON string: ${e.message}", e)
                }
            }
        }
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
     * Generates a request header key/value map of headers to be used on the request
     *   (this method can be overridden as necessary for customizable request headers)
     * @param bearerAuthToken token to be used for Authorization header (use "" for no auth header)
     * @param method the request method type ("GET", "POST", etc)
     * @param requestUrl  requestUrl (optional) used to determine which the request Headers to use
     * @param extraHeaderMap  extraHeaderMap (optional) used to add extra headers that are request-specific
     * @return Map<String,String> of headers to be used for the request
     */
    // TODO - need to confirm that okHttpClient already auto-magically
    //   includes request headers like: "Accept-Encoding: gzip"
    //   i'm pretty sure it does but shouldn't assume....  b/c Elixir sure doesn't !!!  :-p
    protected open fun generateRequestHeaderMap(method: String = "", requestUrl: String = "", bearerAuthToken: String = "", extraHeaderMap: Map<String, String> = emptyMap()): MutableMap<String, String> {
        return mutableMapOf(
            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.USER_AGENT to getUserAgentValue()
        ).apply {
            if (bearerAuthToken.isNotEmpty()) {
                put(HttpHeaders.AUTHORIZATION, "Bearer $bearerAuthToken")
            }
            putAll(extraHeaderMap)
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
     *  Then call 'close' on the okHttp Response
     */
    protected open fun buildServiceResponse(rawResponse: Response): ServiceResponse {
        rawResponse.use { // 'use' will call close() on the rawResponse
            return ServiceResponse(
                httpCode = rawResponse.code,
                body = rawResponse.body?.string() ?: "",
                headerMap = rawResponse.headers.toMap(),
                responseCookies = extractResponseCookies(rawResponse)
            )
        }
    }

    protected fun extractResponseCookies(rawResponse: Response): List<Cookie> {
        return rawResponse.headers.values("Set-Cookie").flatMap(HttpCookie::parse).map {
            Cookie(it.name, it.value).apply {
                isHttpOnly = true
                secure = it.secure
                maxAge = it.maxAge.toInt()
            }
        }
    }
}
