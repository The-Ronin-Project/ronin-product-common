package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.annotation.JsonValue

public enum class ResponseType(
    @JsonValue
    public val `value`: String,
) {
    QUESTIONNAIRE_ASSIGNMENT("QUESTIONNAIRE_ASSIGNMENT"),
    QUESTIONNAIRE_ASSIGNMENT_STATE("QUESTIONNAIRE_ASSIGNMENT_STATE"),
    ERROR_RESPONSE("ERROR_RESPONSE"),
    ;

    public companion object {
        private val mapping: Map<String, ResponseType> = values().associateBy(ResponseType::value)

        public fun fromValue(`value`: String): ResponseType? = mapping[value]
    }
}
