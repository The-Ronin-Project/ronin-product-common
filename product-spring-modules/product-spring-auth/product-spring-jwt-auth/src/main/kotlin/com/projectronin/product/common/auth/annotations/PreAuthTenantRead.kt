package com.projectronin.product.common.auth.annotations

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('SCOPE_admin:read') || hasAuthority('SCOPE_tenant:read')")
annotation class PreAuthTenantRead
