package com.projectronin.product.common.auth

import com.projectronin.auth.RoninAuthentication
import com.projectronin.common.telemetry.Tags
import com.projectronin.common.telemetry.addTags
import io.opentracing.util.GlobalTracer
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter

class AuthContextServletFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val context = determineContext(request)

        try {
            setContext(context)
            chain.doFilter(request, response)
        } finally {
            clearContext(context)
        }
    }

    private fun determineContext(request: HttpServletRequest): Map<String, String?> {
        val auth = request.userPrincipal as? RoninAuthentication ?: return emptyMap()
        val user = auth.roninClaims.user

        return mapOf(
            Tags.RONIN_AUTH_IS_AUTHENTICATED to auth.isAuthenticated.toString(),
            Tags.RONIN_AUTH_USER_TYPE to user?.userType.toString(),
            Tags.RONIN_AUTH_USER_ID to user?.id,
            Tags.RONIN_AUTH_PATIENT_ID to user?.loginProfile?.accessingPatientUdpId,
            Tags.RONIN_AUTH_PROVIDER_ID to user?.loginProfile?.accessingProviderUdpId,
            Tags.TENANT_TAG to user?.loginProfile?.accessingTenantId
        )
    }

    private fun setContext(context: Map<String, String?>) {
        context.forEach(MDC::put)
        GlobalTracer.get().activeSpan().addTags(context)
    }

    private fun clearContext(context: Map<String, String?>) {
        context.keys.forEach(MDC::remove)
    }
}
