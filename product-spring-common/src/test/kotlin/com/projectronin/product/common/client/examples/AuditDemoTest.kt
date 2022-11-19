package com.projectronin.product.common.client.examples

import com.projectronin.product.common.client.AbstractServiceClient
import com.projectronin.product.common.client.auth.AuthBroker
import com.projectronin.product.common.client.auth.PassThruAuthBroker
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

private const val AUDIT_URL = "https://audit.dev.projectronin.io"
private const val AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2NTMzMjgzMjAsImlzcyI6IlNla2kiLCJqdGkiOiIycm9zcW05M2VlbmFwYmlrZm8wMXFrODEiLCJzdWIiOiIxNTFhMjUwOS1lNjllLTQwNDMtYmJhOC1kYmY5ODhkZGE1NTUiLCJ0ZW5hbnRpZCI6ImFwcG9zbmQifQ.gmX_Ad6sgTTW0iogI4kwuhYYbnpn5HGIE5RZxi56Ojs"

class AuditDemoTest {
    /**
     * Example how to make 'real calls' to the Audit Service.
     */
    @Disabled
    @Test
    fun executeAuditDemo() {
        // create new Audit Client
        val auditClient = AuditClient(AUDIT_URL, PassThruAuthBroker(AUTH_TOKEN))

        // make a new audit record we want to persist
        val auditToCreate = Audit(
            resourceCategory = "PATIENT",
            resourceType = "Timeline",
            resourceId = "_patient_id_1234_",
            action = "read",
            mrn = "123456789",
            reportedAt = Instant.now(),
            dataMap = mapOf(
                "requestPath" to "/api/timelines/3/measurements",
                "fakeTrackingId" to "abc1234"
            )
        )

        // make call to save the audit record
        val createdAuditRecordId = auditClient.create(auditToCreate)

        // now make another request to query back the record that was just created
        val fetchedAudit = auditClient.get(createdAuditRecordId)

        assertEquals("_patient_id_1234_", fetchedAudit.resourceId)
    }
}

// /////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////
// Below is how to implement an Audit Client to be used for REST calls
//     This is for demo purposes and would ultimately find a 'better home'
//
private const val AUDIT_PATH = "api/audit"
class AuditClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient()
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "AuditClient/1.0.0"
    }

    fun get(id: UUID): Audit {
        return executeGet("$baseUrl$AUDIT_PATH/$id")
    }

    fun create(audit: Audit): UUID {
        // convert the response body into a generic map,
        //   rather than a specific object .... just to demonstrate that you can
        val keyValueMap: Map<String, Any> = executePost("$baseUrl$AUDIT_PATH", audit)
        return UUID.fromString(keyValueMap.get("id")?.toString() ?: "")
    }
}

// arguably more ideal to reference a class from the
//   Audit Service rather than make our own 'identical copy of the object
//     But this is used for demonstration purposes.
data class Audit(
    val id: UUID? = null,
    var tenantId: String = "",
    var userId: String = "",
    var userFirstName: String = "",
    var userLastName: String = "",
    var userFullName: String = "",
    var resourceCategory: String = "",
    var resourceType: String = "",
    var resourceId: String = "",
    var mrn: String = "",
    var action: String = "",
    var dataMap: Map<String, Any>?,
    var reportedAt: Instant? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null
)
