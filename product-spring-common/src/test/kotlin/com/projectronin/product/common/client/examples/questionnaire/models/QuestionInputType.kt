package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.annotation.JsonValue

public enum class QuestionInputType(
    @JsonValue
    public val `value`: String,
) {
    MULTIPLE_CHOICE_SELECT("MULTIPLE_CHOICE_SELECT"),
    SINGLE_CHOICE_SELECT("SINGLE_CHOICE_SELECT"),
    INTEGER_RANGE("INTEGER_RANGE"),
    FREE_TEXT("FREE_TEXT"),
    ;

    public companion object {
        private val mapping: Map<String, QuestionInputType> =
            values().associateBy(QuestionInputType::value)

        public fun fromValue(`value`: String): QuestionInputType? = mapping[value]
    }
}
