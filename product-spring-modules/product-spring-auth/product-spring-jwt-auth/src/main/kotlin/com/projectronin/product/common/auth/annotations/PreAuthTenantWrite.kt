package com.projectronin.product.common.auth.annotations

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('SCOPE_admin:write') || hasAuthority('SCOPE_tenant:write')")
annotation class PreAuthTenantWrite
