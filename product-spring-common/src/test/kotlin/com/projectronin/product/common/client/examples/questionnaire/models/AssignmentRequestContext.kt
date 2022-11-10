package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class AssignmentRequestContext(
    @param:JsonProperty("patientLabels")
    @get:JsonProperty("patientLabels")
    @get:NotNull
    @get:Valid
    public val patientLabels: Map<String, List<String>>,
)
