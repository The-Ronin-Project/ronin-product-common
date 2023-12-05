package com.projectronin.product.common.jwtwebfluxcontrollertests

import com.projectronin.auth.RoninAuthentication
import com.projectronin.product.common.auth.annotations.PreAuthAdminRead
import com.projectronin.product.common.auth.annotations.PreAuthAdminWrite
import com.projectronin.product.common.auth.annotations.PreAuthEmployeesOnly
import com.projectronin.product.common.auth.annotations.PreAuthPatient
import com.projectronin.product.common.auth.annotations.PreAuthTenantDelete
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/test")
open class JwtWebFluxSimpleController(
    @Autowired val authHolderBean: AuthHolderBean
) {

    @GetMapping("/sample-object", produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getSampleObject(authentication: RoninAuthentication): Mono<ResponseEntity<Any>> {
        return Mono.fromCallable {
            authHolderBean.latestRoninAuth = authentication
            ResponseEntity
                .ok("""{"foo": null}""")
        }
    }

    @GetMapping("/object/{tenant-id}/by/tenant", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("#tenantId == authentication.tenantId")
    open fun getSampleObjectByTenant(@PathVariable("tenant-id", required = true) tenantId: String): Mono<ResponseEntity<Any>> {
        return Mono.just(
            ResponseEntity
                .ok("""{"tenantId": "$tenantId"}""")
        )
    }

    @GetMapping("/object/{tenant-id}/by/tenant/and/{patient-id}/patient", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthPatient
    open fun getSampleObjectByTenantAndPatient(
        @PathVariable("tenant-id", required = true) tenantId: String,
        @PathVariable("patient-id", required = true) patientId: String
    ): Mono<ResponseEntity<Any>> {
        return Mono.just(
            ResponseEntity
                .ok("""{"tenantId": "$tenantId"}""")
        )
    }

    @GetMapping("/object-requiring-role", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('SCOPE_thing_requiring_scope:read')")
    open fun getSampleObjectRequiringRole(): Mono<ResponseEntity<Any>> {
        return Mono.just(
            ResponseEntity
                .ok("""{"foo": null}""")
        )
    }

    @GetMapping("/sample-object-for-employees-only", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthEmployeesOnly
    open fun getSampleObjectForEmployeesOnly(): Mono<ResponseEntity<Any>> {
        return Mono.just(
            ResponseEntity
                .ok("""{"foo": null}""")
        )
    }

    @GetMapping("/object-requiring-admin-read", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthAdminRead
    open fun getSampleObjectForAdminRead(): Mono<ResponseEntity<Any>> {
        return Mono.just(
            ResponseEntity
                .ok("""{"tenant": foo}""")
        )
    }

    @GetMapping("/object-requiring-admin-write", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthAdminWrite
    open fun getSampleObjectForAdminWrite(): Mono<ResponseEntity<Any>> {
        return Mono.just(
            ResponseEntity
                .ok("""{"tenant": foo}""")
        )
    }

    @GetMapping("/object-requiring-tenant-delete", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthTenantDelete
    open fun getSampleObjectForTenantDelete(): Mono<ResponseEntity<Any>> {
        return Mono.just(
            ResponseEntity
                .ok("""{"tenant": foo}""")
        )
    }
}
