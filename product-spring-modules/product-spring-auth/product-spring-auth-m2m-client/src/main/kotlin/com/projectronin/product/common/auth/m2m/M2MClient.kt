package com.projectronin.product.common.auth.m2m

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface M2MClient {
    /**
     * Performs a client_credentials login to Auth0 to get an M2M token
     */
    @POST("{auth-path}/token")
    fun token(
        @Body body: TokenRequest,
        @Path("auth-path") authPath: String
    ): Call<TokenResponse>
}
