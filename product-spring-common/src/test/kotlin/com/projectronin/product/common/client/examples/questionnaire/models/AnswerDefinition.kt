package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import kotlin.Int
import kotlin.String
import kotlin.collections.List

public data class AnswerDefinition(
    @param:JsonProperty("answerDefinitionId")
    @get:JsonProperty("answerDefinitionId")
    @get:NotNull
    @get:Valid
    public val answerDefinitionId: AnswerDefinitionIdentifier,
    @param:JsonProperty("displayText")
    @get:JsonProperty("displayText")
    @get:NotNull
    public val displayText: String,
    @param:JsonProperty("value")
    @get:JsonProperty("value")
    public val `value`: Int? = null,
    @param:JsonProperty("patientEducationResources")
    @get:JsonProperty("patientEducationResources")
    public val patientEducationResources: List<String>? = null,
    @param:JsonProperty("alertTier")
    @get:JsonProperty("alertTier")
    @get:NotNull
    public val alertTier: AnswerDefinitionAlertTier,
    @param:JsonProperty("action")
    @get:JsonProperty("action")
    @get:Valid
    public val action: Action? = null,
    @param:JsonProperty("exportedTags")
    @get:JsonProperty("exportedTags")
    @get:Size(min = 1)
    public val exportedTags: List<String>? = null,
)
