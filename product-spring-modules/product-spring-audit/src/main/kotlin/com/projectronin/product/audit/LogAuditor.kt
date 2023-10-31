package com.projectronin.product.audit

import com.projectronin.auth.RoninAuthentication
import com.projectronin.product.audit.config.AuditProperties
import mu.KLogger
import mu.KotlinLogging

class LogAuditor(private val auditProperties: AuditProperties) : Auditor {
    private val logger: KLogger = KotlinLogging.logger { }

    override fun writeAudit(
        action: String,
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>?,
        mrn: String?,
        roninAuthentication: RoninAuthentication?
    ) {
        logger.info(
            "Audit entry logged for ${auditProperties.sourceService}: " +
                "$action resource $resourceCategory:$resourceType:$resourceId"
        )
    }
}
