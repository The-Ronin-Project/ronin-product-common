package com.projectronin.product.common.auth.seki.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.client.AbstractServiceClient
import com.projectronin.product.common.client.ServiceResponse
import com.projectronin.product.common.client.auth.NoOpAuthBroker
import com.projectronin.product.common.client.exception.ServiceClientException
import okhttp3.OkHttpClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.http.HttpHeaders
import javax.servlet.http.Cookie

private const val VALIDATE_PATH = "session/validate"
private const val HEALTH_PATH = "health"

class SekiClient(
    hostUrl: String,
    client: OkHttpClient = defaultOkHttpClient(),
    objectMapper: ObjectMapper = defaultMapper(),
) :
    AbstractServiceClient(hostUrl, NoOpAuthBroker, client, objectMapper), HealthIndicator {

    override fun getUserAgentValue(): String {
        return "SekiClient/1.0.0"
    }

    /**
     * Validates a token string with seki authentication
     * @return The seki user and session
     * @throws ServiceClientException
     */
    @Throws(ServiceClientException::class)
    fun validate(token: String): AuthResponse {
        return executeGet("$baseUrl/$VALIDATE_PATH?token=$token")
    }

    // todo: it is possible to return return both the body auth response and cookies.
    //   need to think about which syntax seems the best
    @Throws(ServiceClientException::class)
    fun getCookies(referrer: String, state: String): List<Cookie> {
        val resp = executeRequest(
            makeGetRequest(
                url = "$baseUrl/$VALIDATE_PATH?referrer=$referrer",
                extraHeaderMap = mapOf("X-STATE" to state)
            )
        )
        return resp.responseCookies
    }

    /**
     * Invokes the seki health endpoint
     * @return An actuator object representing seki connectivity
     */
    override fun health(): Health {
        try {
            val resp = executeRequest(makeGetRequest(url = "$baseUrl/$HEALTH_PATH", shouldThrowOnStatusError = false)) /* don't throw exception on error http status */
            val isAlive = isAliveResponse(resp)
            return Health.Builder()
                .withDetail("httpStatus", resp.httpStatus)
                .apply {
                    if (isAlive) {
                        up()
                    } else {
                        logger.error { "Failed Seki healthcheck" }
                        withDetail("httpResponse", resp.body)
                        down()
                    }
                }
                .build()
        } catch (e: Exception) {
            logger.error(e) { "Failed Seki healthcheck request" }
            return Health.Builder().withException(e.cause).down().build()
        }
    }

    /**
     * @inheritDoc
     */
    override fun generateRequestHeaderMap(method: String, requestUrl: String, extraHeaderMap: Map<String, String>): MutableMap<String, String> {
        // override the default 'Accept' header to avoid 406 seki error
        return super.generateRequestHeaderMap(method, requestUrl, extraHeaderMap + mapOf(HttpHeaders.ACCEPT to "*/*"))
    }

    /**
     * Check if the response indicates 'alive=true'
     */
    private fun isAliveResponse(serviceResponse: ServiceResponse): Boolean {
        if (serviceResponse.httpStatus.is2xxSuccessful) {
            val sekiHealth: SekiHealth = convertStringToObject(serviceResponse.body)
            return sekiHealth.alive
        }
        return false
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class SekiHealth(val alive: Boolean)
}
