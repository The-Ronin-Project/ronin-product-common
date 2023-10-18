package com.projectronin.product.contracttest.database

import java.util.UUID

abstract class DeleteBuilder(
    /**
     * a list of pairs of table name to id field name
     */
    private val deleteTableOrder: List<Pair<String, String>>
) {
    private val records = mutableListOf<Record<Any>>()

    /**
     * allows you to do += record
     */
    operator fun <T> plusAssign(record: T) {
        addRecord(records, record)
    }

    protected abstract fun <T> addRecord(records: MutableList<Record<Any>>, record: T)

    /**
     * builds the sql statements needed to clean up the database
     */
    fun build() = deleteTableOrder.mapNotNull { buildDeleteSqlOrNull(it.first, it.second) }

    private fun buildDeleteSqlOrNull(
        tableName: String,
        idFieldName: String
    ): String? =
        records.filter { it.tableName == tableName }
            .map { formatValue(it.id) }
            .distinct()
            .takeUnless { it.isEmpty() }
            ?.joinToString(",", "DELETE FROM $tableName WHERE $idFieldName IN (", ");")

    private fun formatValue(value: Any?): String = when (value) {
        is String, is UUID -> "'$value'"
        is Long, is Int -> "$value"
        else -> error("unsupported value type")
    }

    data class Record<T>(
        val id: T,
        val tableName: String
    )
}
