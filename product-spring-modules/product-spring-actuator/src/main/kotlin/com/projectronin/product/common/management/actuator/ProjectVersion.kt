package com.projectronin.product.common.management.actuator

data class ProjectVersion(
    val version: String?,
    val lastTag: String?,
    val commitDistance: Int?,
    val gitHash: String?,
    val gitHashFull: String?,
    val branchName: String?,
    val dirty: Boolean?
)
