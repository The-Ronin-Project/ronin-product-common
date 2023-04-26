package com.projectronin.product.common.auth.annotations

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("#tenantId == authentication.tenantId && #patientId == (authentication.patientRoninId != null ? authentication.patientRoninId : #patientId)")
annotation class PreAuthPatient
