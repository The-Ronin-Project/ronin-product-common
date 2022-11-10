package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class RequiredTagsOperator(
    @JsonValue
    public val `value`: String,
) {
    AND("AND"),
    OR("OR"),
    ;

    public companion object {
        private val mapping: Map<String, RequiredTagsOperator> =
            values().associateBy(RequiredTagsOperator::value)

        public fun fromValue(`value`: String): RequiredTagsOperator? = mapping[value]
    }
}
