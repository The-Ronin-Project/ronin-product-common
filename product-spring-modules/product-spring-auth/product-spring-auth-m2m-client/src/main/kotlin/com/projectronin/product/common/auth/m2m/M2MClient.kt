package com.projectronin.product.common.auth.m2m

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface M2MClient {
    /**
     * Performs a client_credentials login to Auth0 to get an M2M token
     */
    @POST("oauth/token")
    fun token(
        @Body body: TokenRequest
    ): Call<TokenResponse>
}
