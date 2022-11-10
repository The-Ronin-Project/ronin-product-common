package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull

public data class QuestionState(
    @param:JsonProperty("question")
    @get:JsonProperty("question")
    @get:NotNull
    @get:Valid
    public val question: Question,
    @param:JsonProperty("answer")
    @get:JsonProperty("answer")
    @get:Valid
    public val answer: Answer? = null,
)
