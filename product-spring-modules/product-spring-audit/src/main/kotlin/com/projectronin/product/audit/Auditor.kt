package com.projectronin.product.audit

interface Auditor {
    fun read(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    ) {
        writeAudit("READ", resourceCategory, resourceType, resourceId, dataMap, mrn)
    }

    fun create(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    ) {
        writeAudit("CREATE", resourceCategory, resourceType, resourceId, dataMap, mrn)
    }

    fun update(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    ) {
        writeAudit("UPDATE", resourceCategory, resourceType, resourceId, dataMap, mrn)
    }

    fun delete(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    ) {
        writeAudit("DELETE", resourceCategory, resourceType, resourceId, dataMap, mrn)
    }

    @Suppress("complexity:LongParameterList")
    fun writeAudit(
        action: String,
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    )
}
