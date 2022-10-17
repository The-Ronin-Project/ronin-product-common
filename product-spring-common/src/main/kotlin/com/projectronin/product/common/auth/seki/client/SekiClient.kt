package com.projectronin.product.common.auth.seki.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.auth.seki.client.exception.SekiClientException
import com.projectronin.product.common.auth.seki.client.exception.SekiInvalidTokenException
import com.projectronin.product.common.auth.seki.client.model.AuthResponse
import com.projectronin.product.common.auth.seki.client.model.SekiHealth
import com.projectronin.product.common.config.JsonProvider
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

private const val VALIDATE_PATH = "session/validate"
private const val HEALTH_PATH = "health"
private const val INVALID_TOKEN_RESPONSE = "{\"error\":\"Unauthorized\"}"

/**
 * Prebuilt client for authenticating with seki
 *
 * @param sekiUrl The base URL for seki
 * @param client The HTTP client
 * @param objectMapper The [ObjectMapper] to use for serialization
 */
class SekiClient(
    sekiUrl: String,
    private val client: OkHttpClient,
    private val objectMapper: ObjectMapper = JsonProvider.objectMapper
) : HealthIndicator {
    private val logger = KotlinLogging.logger { }
    private val baseUrl = if (sekiUrl.endsWith("/")) sekiUrl else "$sekiUrl/"

    /**
     * Validates a token string with seki authentication
     *
     * @return The seki user and session
     * @throws SekiInvalidTokenException if the token provided was invalid
     * @throws SekiClientException if an unexpected status returned or exception was thrown
     */
    @Throws(SekiClientException::class)
    fun validate(token: String): AuthResponse {

        val requestUrl = "$baseUrl$VALIDATE_PATH?token=$token"
        val request = Request.Builder()
            .url(requestUrl)
            .addHeader(HttpHeaders.USER_AGENT, "SekiClient/1.0.0")
            .build()

        try {
            client.newCall(request).execute().use {

                val responseStatus = HttpStatus.valueOf(it.code)
                val responseString = it.body?.string() ?: ""

                // NOTE: A 401 response could be 2 different scenarios
                //   a) an actual invalid token response
                //   b) this service was unauthorized when attempting to connect to seki service
                if (responseStatus == HttpStatus.OK)
                    return objectMapper.readValue(responseString, AuthResponse::class.java)
                else if (HttpStatus.UNAUTHORIZED == responseStatus && responseString == INVALID_TOKEN_RESPONSE)
                    throw SekiInvalidTokenException("Token was invalid")
                else
                    throw SekiClientException(generateExceptionMessage(responseStatus, responseString))
            }
        } catch (ex: SekiClientException) {
            throw ex
        } catch (ex: Exception) {
            throw SekiClientException("Seki Error: ${ex.message}", cause = ex)
        }
    }

    private fun generateExceptionMessage(responseStatus: HttpStatus, responseBody: String): String {
        val singleLineResponse = responseBody.replace("\n", ", ")
        return "Seki Error: Unexpected error while fetching token: [${responseStatus.value()}] $singleLineResponse"
    }

    /**
     * Invokes the seki health endpoint
     *
     * @return An actuator object representing seki connectivity
     */
    override fun health(): Health {
        val request = Request.Builder()
            .url("$baseUrl$HEALTH_PATH")
            .addHeader(HttpHeaders.USER_AGENT, "SekiClient/1.0.0")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseStatus = HttpStatus.valueOf(response.code)
                val responseString = response.body?.string() ?: ""

                Health.Builder()
                    .withDetail("httpStatus", responseStatus)
                    .run {
                        if (responseStatus.is2xxSuccessful) {
                            val sekiHealth = objectMapper.readValue(responseString, SekiHealth::class.java)
                            if (sekiHealth.alive) {
                                up()
                            } else {
                                down()
                            }
                        } else {
                            logger.error { "Failed Seki healthcheck" }
                            withDetail("httpResponse", responseString).down()
                        }
                    }
                    .build()
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed Seki healthcheck request" }
            Health.Builder().withException(e).down().build()
        }
    }
}
