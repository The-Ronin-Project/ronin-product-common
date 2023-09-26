package com.projectronin.product.common.auth.m2m

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.auth.m2m.M2MTokenException
import com.projectronin.auth.m2m.M2MTokenProviderBase
import com.projectronin.auth.m2m.TokenRequest
import com.projectronin.auth.m2m.TokenResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.time.Clock

class M2MClientService(
    httpClient: OkHttpClient,
    objectMapper: ObjectMapper,
    providerUrl: String,
    clientId: String,
    clientSecret: String,
    clock: () -> Clock = { Clock.systemUTC() },
    authPath: String = "oauth",
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : M2MTokenProviderBase(
    clientId,
    clientSecret,
    authPath,
    clock,
    coroutineDispatcher
) {
    private val logger = KotlinLogging.logger { }

    private val client = Retrofit.Builder()
        .baseUrl(if (providerUrl.endsWith("/")) providerUrl else "$providerUrl/") // NOTE IT HAS TO HAVE A TRAILING SLASH
        .client(httpClient)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
        .create(M2MClient::class.java)

    override fun getNewToken(tokenRequest: TokenRequest, authPath: String): Result<TokenResponse> {
        val response = client.token(tokenRequest, authPath).execute()

        return if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            if (response.errorBody() != null) {
                val errorBody = response.errorBody()!!.string()
                logger.error { "Unable to get new token: $errorBody" }
            }

            Result.failure(M2MTokenException("Unable to get new token"))
        }
    }
}

private interface M2MClient {
    /**
     * Performs a client_credentials login to Auth0 to get an M2M token
     */
    @POST("{auth-path}/token")
    fun token(
        @Body body: TokenRequest,
        @Path("auth-path") authPath: String
    ): Call<TokenResponse>
}
