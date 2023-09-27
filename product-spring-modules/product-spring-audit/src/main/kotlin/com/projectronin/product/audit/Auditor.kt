package com.projectronin.product.audit

interface Auditor {
    fun read(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    )

    fun create(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    )

    fun update(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    )

    fun delete(
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    )

    fun writeAudit(
        action: String,
        resourceCategory: String,
        resourceType: String,
        resourceId: String,
        dataMap: Map<String, Any>? = null,
        mrn: String? = null
    )
}
