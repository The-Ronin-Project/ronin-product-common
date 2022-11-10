package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.Size
import kotlin.String
import kotlin.collections.List

public data class RequiredTags(
    @param:JsonProperty("operator")
    @get:JsonProperty("operator")
    public val `operator`: RequiredTagsOperator? = null,
    @param:JsonProperty("tags")
    @get:JsonProperty("tags")
    @get:Size(min = 1)
    public val tags: List<String>? = null,
)
