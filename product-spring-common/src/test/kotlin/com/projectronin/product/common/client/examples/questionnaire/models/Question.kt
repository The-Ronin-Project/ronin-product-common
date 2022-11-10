package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull
import kotlin.Int
import kotlin.String
import kotlin.collections.List

public data class Question(
    @param:JsonProperty("questionId")
    @get:JsonProperty("questionId")
    @get:NotNull
    @get:Valid
    public val questionId: QuestionIdentifier,
    @param:JsonProperty("inputType")
    @get:JsonProperty("inputType")
    @get:NotNull
    public val inputType: QuestionInputType,
    @param:JsonProperty("patientSummaryTemplate")
    @get:JsonProperty("patientSummaryTemplate")
    public val patientSummaryTemplate: String? = null,
    @param:JsonProperty("providerSummaryTemplate")
    @get:JsonProperty("providerSummaryTemplate")
    public val providerSummaryTemplate: String? = null,
    @param:JsonProperty("questionText")
    @get:JsonProperty("questionText")
    @get:NotNull
    public val questionText: String,
    @param:JsonProperty("numericMin")
    @get:JsonProperty("numericMin")
    public val numericMin: Int? = null,
    @param:JsonProperty("numericMax")
    @get:JsonProperty("numericMax")
    public val numericMax: Int? = null,
    @param:JsonProperty("domain")
    @get:JsonProperty("domain")
    public val domain: String? = null,
    @param:JsonProperty("answerDefinitions")
    @get:JsonProperty("answerDefinitions")
    @get:NotNull
    @get:Valid
    public val answerDefinitions: List<AnswerDefinition>,
)
