package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

public data class QuestionGroupIdentifier(
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    public val id: String? = null,
    @param:JsonProperty("version")
    @get:JsonProperty("version")
    @get:NotNull
    @get:Pattern(regexp = "[0-9a-f]+")
    public val version: String,
)
