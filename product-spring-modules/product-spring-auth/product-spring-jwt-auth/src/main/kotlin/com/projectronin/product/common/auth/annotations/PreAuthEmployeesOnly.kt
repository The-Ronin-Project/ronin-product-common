package com.projectronin.product.common.auth.annotations

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("authentication.roninClaims.user?.userType?.toString() == 'RONIN_EMPLOYEE'")
annotation class PreAuthEmployeesOnly
