package com.projectronin.product.common.auth.seki.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.client.AbstractServiceClient
import com.projectronin.product.common.client.ServiceResponse
import com.projectronin.validation.clinical.data.client.work.auth.NoOpAuthBroker
import com.projectronin.validation.clinical.data.client.work.exception.ServiceClientException
import okhttp3.OkHttpClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.http.HttpHeaders

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
        return executeGet("$baseUrl$VALIDATE_PATH?token=$token")
    }

    /**
     * Invokes the seki health endpoint
     * @return An actuator object representing seki connectivity
     */
    override fun health(): Health {
        try {
            val resp = executeRawGet("$baseUrl$HEALTH_PATH", false /* don't throw exception on error http status */)
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
    override fun getRequestHeaderMap(bearerAuthToken: String, method: String, requestUrl: String): MutableMap<String, String> {
        // override default 'Accept' header to avoid 406 seki error
        return super.getRequestHeaderMap(bearerAuthToken, method, requestUrl).apply { put(HttpHeaders.ACCEPT, "*/*") }
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
