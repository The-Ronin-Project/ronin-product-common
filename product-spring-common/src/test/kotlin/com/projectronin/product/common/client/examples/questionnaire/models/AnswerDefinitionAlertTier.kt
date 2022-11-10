package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class AnswerDefinitionAlertTier(
    @JsonValue
    public val `value`: String,
) {
    NOT_APPLICABLE("NOT_APPLICABLE"),
    LOW("LOW"),
    INTERMEDIATE("INTERMEDIATE"),
    HIGH("HIGH"),
    EXTREME("EXTREME"),
    ;

    public companion object {
        private val mapping: Map<String, AnswerDefinitionAlertTier> =
            values().associateBy(AnswerDefinitionAlertTier::value)

        public fun fromValue(`value`: String): AnswerDefinitionAlertTier? = mapping[value]
    }
}
