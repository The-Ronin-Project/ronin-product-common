package com.projectronin.product.common.auth.m2m

import com.fasterxml.jackson.databind.ObjectMapper
import com.projectronin.product.common.auth.token.RoninLoginProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.internal.toImmutableList
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap

const val IMPERSONATE_TENANT_PREFIX: String = "impersonate_tenant"
const val IMPERSONATE_PROVIDER_PREFIX: String = "impersonate_provider"
const val IMPERSONATE_PATIENT_PREFIX: String = "impersonate_patient"
const val WILDCARD_SUFFIX: String = "any"

interface M2MTokenProvider {

    fun getToken(
        audience: String,
        scopes: List<String>? = null,
        requestedProfile: RoninLoginProfile? = null
    ): String

    fun addTokenListener(
        audience: String,
        scopes: List<String>? = null,
        requestedProfile: RoninLoginProfile? = null,
        listener: TokenListener
    )

    fun removeTokenListener(
        audience: String,
        scopes: List<String>? = null,
        requestedProfile: RoninLoginProfile? = null,
        listener: TokenListener
    )

    fun impersonateTenantScope(tenantId: String) = "$IMPERSONATE_TENANT_PREFIX:$tenantId"

    fun impersonateProviderScope(providerId: String) = "$IMPERSONATE_PROVIDER_PREFIX:$providerId"

    fun impersonateAnyProviderScope() = "$IMPERSONATE_PROVIDER_PREFIX:$WILDCARD_SUFFIX"

    fun impersonatePatientScope(patientId: String) = "$IMPERSONATE_PATIENT_PREFIX:$patientId"

    fun impersonateAnyPatientScope() = "$IMPERSONATE_PATIENT_PREFIX:$WILDCARD_SUFFIX"
}

class M2MClientService(
    private val client: M2MClient,
    private val clientId: String,
    private val clientSecret: String,
    private val clock: () -> Clock = { Clock.systemUTC() },
    private val authPath: String = "oauth",
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : M2MTokenProvider {

    private val logger = KotlinLogging.logger { }

    constructor(
        httpClient: OkHttpClient,
        objectMapper: ObjectMapper,
        providerUrl: String,
        clientId: String,
        clientSecret: String,
        clock: () -> Clock = { Clock.systemUTC() },
        authPath: String = "oauth",
        coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(
        client = Retrofit.Builder()
            .baseUrl(if (providerUrl.endsWith("/")) providerUrl else "$providerUrl/") // NOTE IT HAS TO HAVE A TRAILING SLASH
            .client(httpClient)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()
            .create(M2MClient::class.java),
        clientId = clientId,
        clientSecret = clientSecret,
        clock = clock,
        authPath = authPath,
        coroutineDispatcher = coroutineDispatcher
    )

    private val tokenCache: ConcurrentHashMap<String, TokenResponse> = ConcurrentHashMap()

    private val tokenListeners: ConcurrentHashMap<String, List<TokenListener>> = ConcurrentHashMap()

    /**
     * Request a token for a specific set of requirements.  If the token isn't locally cached, it will be requested from the
     * auth0 endpoint.  Be prepared for auth exceptions for bad credentials, etc.
     *
     * Tokens will be cached locally for 90% of their expiration time, but won't be re-acquired at that time.  After cache eviction, the next
     * request for a token with the same inputs will read through to a new request to auth0.
     *
     * Most services should not need to request a profile.  The profile allows auth0 to produce a token that contains a profile listing a specific tenant,
     * patient, or provider.  This is meant for E2E and integration tests, where a service (the integration test) is requesting the ability to access
     * the system as (say) a provider would, with a specific tenant/patient/provider.  You can consider this a type of impersonation, where a service
     * is acting as a specific provider.
     *
     * When requesting a profile, this service will check the input scopes to make sure that scopes specific to the profile request are added to the
     * request so that auth0 may verify that the client is actually allowed to do so.  It will examine the requested scopes for specific scopes:
     *
     *     impersonate_tenant:[tenant id]
     *     impersonate_provider:[provider id|any]
     *     impersonate_patient:[patient id|any]
     *
     * If no valid scopes are in the list, it will add the "id" forms, requesting specific access to the tenant/provider/patient impersonation resources.  It
     * will request a scope for _both_ the ronin patient identifier and the external patient identifier
     *
     * In addition, this service will reject requests for a profile that do not include a tenant.
     *
     * @param audience         The API you are calling.  This must match the "API Audience" value on the APIs list in auth0 EXACTLY.
     * @param scopes           An optional list of scopes.  They must be valid scopes on the API definition in auth0 and must have been granted to the application in auth0
     * @param requestedProfile A tenant/provider/patient to impersonate.  This is designed solely for integration testing, and specifies the loginProfile that will appear in the
     *                         ronin claims for the token.  The above scopes must have been granted to the application by the API in auth0, or this request will fail as unauthorized.
     */
    override fun getToken(
        audience: String,
        scopes: List<String>?,
        requestedProfile: RoninLoginProfile?
    ): String {
        val key = cacheKey(audience, scopes, requestedProfile)
        return (
            tokenCache.computeIfPresent(key) { _, currentValue ->
                if (currentValue.expiresAt < clock().instant()) {
                    null
                } else {
                    currentValue
                }
            } ?: tokenCache.computeIfAbsent(key) { _ ->
                val token = getNewToken(audience, scopes, requestedProfile)
                CoroutineScope(coroutineDispatcher).launch {
                    notifyListeners(key, token)
                }
                token
            }
            ).accessToken
    }

    private fun notifyListeners(cacheKey: String, newToken: TokenResponse) {
        tokenListeners[cacheKey]?.let { listeners -> listeners.forEach { listener -> listener.tokenChanged(newToken) } }
    }

    override fun addTokenListener(
        audience: String,
        scopes: List<String>?,
        requestedProfile: RoninLoginProfile?,
        listener: TokenListener
    ) {
        tokenListeners.compute(cacheKey(audience, scopes, requestedProfile)) { _, currentListenerList ->
            (currentListenerList ?: emptyList()) + listener
        }
    }

    override fun removeTokenListener(
        audience: String,
        scopes: List<String>?,
        requestedProfile: RoninLoginProfile?,
        listener: TokenListener
    ) {
        tokenListeners.computeIfPresent(cacheKey(audience, scopes, requestedProfile)) { _, currentListenerList ->
            val newList = currentListenerList - listener
            newList.ifEmpty { null }
        }
    }

    private fun getNewToken(
        audience: String,
        scopes: List<String>?,
        requestedProfile: RoninLoginProfile?
    ): TokenResponse {
        val finalScopeList: List<String>? = if (requestedProfile != null) {
            val newScopeList = mutableListOf<String>()
            scopes?.also { newScopeList.addAll(it) }
            when (val tenantId = requestedProfile.accessingTenantId) {
                null -> throw M2MImpersonationException("If requesting a profile, accessingTenantId must be specified")
                else -> addScopeIfAbsent(listOf(impersonateTenantScope(tenantId)), newScopeList, impersonateTenantScope(tenantId))
            }
            requestedProfile.accessingProviderUdpId?.also { providerUdpId ->
                addScopeIfAbsent(
                    listOf(impersonateAnyProviderScope(), impersonateProviderScope(providerUdpId)),
                    newScopeList,
                    impersonateProviderScope(providerUdpId)
                )
            }
            requestedProfile.accessingPatientUdpId?.also { patientUdpId ->
                addScopeIfAbsent(
                    listOf(impersonateAnyPatientScope(), impersonatePatientScope(patientUdpId)),
                    newScopeList,
                    impersonatePatientScope(patientUdpId)
                )
            }
            requestedProfile.accessingExternalPatientId?.also { externalPatientId ->
                addScopeIfAbsent(
                    listOf(impersonateAnyPatientScope(), impersonatePatientScope(externalPatientId)),
                    newScopeList,
                    impersonatePatientScope(externalPatientId)
                )
            }
            newScopeList.toImmutableList()
        } else {
            scopes
        }

        val response = client.token(
            TokenRequest(
                clientId = clientId,
                clientSecret = clientSecret,
                audience = audience,
                scopes = finalScopeList,
                requestedProfile = requestedProfile
            ),
            authPath = authPath
        ).execute()

        return if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            if (response.errorBody() != null) {
                val errorBody = response.errorBody()!!.string()
                logger.error { "Unable to get new token: $errorBody" }
            }

            throw M2MTokenException("Unable to get new token")
        }
    }

    private fun cacheKey(
        audience: String,
        scopes: List<String>?,
        requestedProfile: RoninLoginProfile?
    ): String = "$audience:$scopes:${requestedProfile?.accessingTenantId}:${requestedProfile?.accessingProviderUdpId}:${requestedProfile?.accessingPatientUdpId}"

    private fun addScopeIfAbsent(requiredScopes: List<String>, requestedScopes: MutableList<String>, defaultScope: String) {
        if (!requiredScopes.any { requiredScope -> requestedScopes.contains(requiredScope) }) {
            requestedScopes += defaultScope
        }
    }
}
