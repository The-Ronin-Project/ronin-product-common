package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import java.time.OffsetDateTime
import javax.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class ErrorResponseError(
    @param:JsonProperty("httpStatus")
    @get:JsonProperty("httpStatus")
    public val httpStatus: String? = null,
    @param:JsonProperty("timestamp")
    @get:JsonProperty("timestamp")
    public val timestamp: OffsetDateTime? = null,
    @param:JsonProperty("status")
    @get:JsonProperty("status")
    @get:NotNull
    public val status: Int,
    @param:JsonProperty("error")
    @get:JsonProperty("error")
    public val error: String? = null,
    @param:JsonProperty("exception")
    @get:JsonProperty("exception")
    public val exception: String? = null,
    @param:JsonProperty("message")
    @get:JsonProperty("message")
    public val message: String? = null,
    @param:JsonProperty("detail")
    @get:JsonProperty("detail")
    public val detail: String? = null,
    @param:JsonProperty("stackTrace")
    @get:JsonProperty("stackTrace")
    public val stackTrace: String? = null,
)
