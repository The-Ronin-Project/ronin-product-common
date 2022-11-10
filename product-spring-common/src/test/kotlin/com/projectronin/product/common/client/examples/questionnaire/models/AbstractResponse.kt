package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "responseType",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = QuestionnaireAssignmentResponse::class,
        name =
        "QUESTIONNAIRE_ASSIGNMENT"
    ),
    JsonSubTypes.Type(
        value =
        QuestionnaireAssignmentStateResponse::class,
        name =
        "QUESTIONNAIRE_ASSIGNMENT_STATE"
    ),
    JsonSubTypes.Type(
        value = ErrorResponse::class,
        name =
        "ERROR_RESPONSE"
    )
)
public sealed class AbstractResponse(
    public open val meta: Map<String, Any>? = null,
) {
    public abstract val responseType: ResponseType
}

public data class QuestionnaireAssignmentResponse(
    @param:JsonProperty("meta")
    @get:JsonProperty("meta")
    public override val meta: Map<String, Any>? = null,
    @param:JsonProperty("data")
    @get:JsonProperty("data")
    @get:NotNull
    @get:Valid
    public val `data`: QuestionnaireAssignment,
) : AbstractResponse(meta) {
    @get:JsonProperty("responseType")
    @get:NotNull
    public override val responseType: ResponseType = ResponseType.QUESTIONNAIRE_ASSIGNMENT
}

public data class QuestionnaireAssignmentStateResponse(
    @param:JsonProperty("meta")
    @get:JsonProperty("meta")
    public override val meta: Map<String, Any>? = null,
    @param:JsonProperty("data")
    @get:JsonProperty("data")
    @get:NotNull
    @get:Valid
    public val `data`: QuestionnaireAssignmentState,
) : AbstractResponse(meta) {
    @get:JsonProperty("responseType")
    @get:NotNull
    public override val responseType: ResponseType = ResponseType.QUESTIONNAIRE_ASSIGNMENT_STATE
}

public data class ErrorResponse(
    @param:JsonProperty("meta")
    @get:JsonProperty("meta")
    public override val meta: Map<String, Any>? = null,
    @param:JsonProperty("error")
    @get:JsonProperty("error")
    @get:NotNull
    @get:Size(min = 1)
    @get:Valid
    public val error: List<ErrorResponseError>,
) : AbstractResponse(meta) {
    @get:JsonProperty("responseType")
    @get:NotNull
    public override val responseType: ResponseType = ResponseType.ERROR_RESPONSE
}
