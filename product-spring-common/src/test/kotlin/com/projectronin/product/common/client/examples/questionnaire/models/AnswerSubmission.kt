package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

public data class AnswerSubmission(
    @param:JsonProperty("completed")
    @get:JsonProperty("completed")
    @get:NotNull
    public val completed: Boolean,
    @param:JsonProperty("answers")
    @get:JsonProperty("answers")
    @get:NotNull
    @get:Size(min = 1)
    @get:Valid
    public val answers: List<Answer>,
)
