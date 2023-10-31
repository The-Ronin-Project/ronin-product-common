package com.projectronin.product.audit

import com.projectronin.auth.RoninAuthentication

interface Auditor {
    fun read(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null,
        roninAuthentication: RoninAuthentication? = null
    ) {
        writeAudit("READ", resourceCategory, resourceType, resourceId, dataMap, mrn, roninAuthentication)
    }

    fun create(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null,
        roninAuthentication: RoninAuthentication? = null
    ) {
        writeAudit("CREATE", resourceCategory, resourceType, resourceId, dataMap, mrn, roninAuthentication)
    }

    fun update(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null,
        roninAuthentication: RoninAuthentication? = null
    ) {
        writeAudit("UPDATE", resourceCategory, resourceType, resourceId, dataMap, mrn, roninAuthentication)
    }

    fun delete(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null,
        roninAuthentication: RoninAuthentication? = null
    ) {
        writeAudit("DELETE", resourceCategory, resourceType, resourceId, dataMap, mrn, roninAuthentication)
    }

    @Suppress("complexity:LongParameterList")
    fun writeAudit(
        action: String,
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null,
        roninAuthentication: RoninAuthentication? = null
    )
}
