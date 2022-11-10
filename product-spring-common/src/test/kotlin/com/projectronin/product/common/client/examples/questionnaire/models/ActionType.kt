package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ActionType(
    @JsonValue
    public val `value`: String,
) {
    JUMP_TO_QUESTION("JUMP_TO_QUESTION"),
    NEXT_QUESTION("NEXT_QUESTION"),
    END_QUESTION_GROUP("END_QUESTION_GROUP"),
    ;

    public companion object {
        private val mapping: Map<String, ActionType> = values().associateBy(ActionType::value)

        public fun fromValue(`value`: String): ActionType? = mapping[value]
    }
}
