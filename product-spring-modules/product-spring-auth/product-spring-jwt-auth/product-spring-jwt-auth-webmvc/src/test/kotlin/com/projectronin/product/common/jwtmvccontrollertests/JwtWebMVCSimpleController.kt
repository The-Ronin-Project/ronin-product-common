package com.projectronin.product.common.jwtmvccontrollertests

import com.projectronin.auth.RoninAuthentication
import com.projectronin.product.common.auth.annotations.PreAuthEmployeesOnly
import com.projectronin.product.common.auth.annotations.PreAuthPatient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
open class JwtWebMVCSimpleController(
    @Autowired val authHolderBean: AuthHolderBean
) {

    @GetMapping("/sample-object", produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getSampleObject(authentication: RoninAuthentication): ResponseEntity<Any> {
        authHolderBean.latestRoninAuth = authentication
        return ResponseEntity
            .ok("""{"foo": null}""")
    }

    @GetMapping("/object/{tenant-id}/by/tenant", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("#tenantId == authentication.tenantId")
    open fun getSampleObjectByTenant(@PathVariable("tenant-id", required = true) tenantId: String): ResponseEntity<Any> {
        return ResponseEntity
            .ok("""{"tenantId": "$tenantId"}""")
    }

    @GetMapping("/object/{tenant-id}/by/tenant/and/{patient-id}/patient", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthPatient
    open fun getSampleObjectByTenantAndPatient(
        @PathVariable("tenant-id", required = true) tenantId: String,
        @PathVariable("patient-id", required = true) patientId: String
    ): ResponseEntity<Any> {
        return ResponseEntity
            .ok("""{"tenantId": "$tenantId"}""")
    }

    @GetMapping("/object-requiring-role", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('SCOPE_thing_requiring_scope:read')")
    open fun getSampleObjectRequiringRole(): ResponseEntity<Any> {
        return ResponseEntity
            .ok("""{"foo": null}""")
    }

    @GetMapping("/sample-object-for-employees-only", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthEmployeesOnly
    open fun getSampleObjectForEmployeesOnly(): ResponseEntity<Any> {
        return ResponseEntity
            .ok("""{"foo": null}""")
    }
}
