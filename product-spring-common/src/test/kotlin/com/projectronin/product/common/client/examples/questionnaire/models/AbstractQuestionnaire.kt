package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "__type",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = QuestionnaireState::class,
        name =
        "QuestionnaireState"
    ),
    JsonSubTypes.Type(value = Questionnaire::class, name = "Questionnaire")
)
public sealed class AbstractQuestionnaire(
    public open val questionnaireId: String,
    public open val title: String? = null,
    public open val description: String? = null,
    public open val questionnaireType: String? = null,
    public open val purpose: String? = null,
) {
    public abstract val type: String
}

public data class QuestionnaireState(
    @param:JsonProperty("questionnaireId")
    @get:JsonProperty("questionnaireId")
    @get:NotNull
    public override val questionnaireId: String,
    @param:JsonProperty("title")
    @get:JsonProperty("title")
    public override val title: String? = null,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    public override val description: String? = null,
    @param:JsonProperty("questionnaireType")
    @get:JsonProperty("questionnaireType")
    public override val questionnaireType: String? = null,
    @param:JsonProperty("purpose")
    @get:JsonProperty("purpose")
    public override val purpose: String? = null,
    @param:JsonProperty("questionGroups")
    @get:JsonProperty("questionGroups")
    @get:NotNull
    @get:Valid
    public val questionGroups: List<QuestionGroupState>,
) : AbstractQuestionnaire(questionnaireId, title, description, questionnaireType, purpose) {
    @get:JsonProperty("__type")
    @get:NotNull
    public override val type: String = "QuestionnaireState"
}

public data class Questionnaire(
    @param:JsonProperty("questionnaireId")
    @get:JsonProperty("questionnaireId")
    @get:NotNull
    public override val questionnaireId: String,
    @param:JsonProperty("title")
    @get:JsonProperty("title")
    public override val title: String? = null,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    public override val description: String? = null,
    @param:JsonProperty("questionnaireType")
    @get:JsonProperty("questionnaireType")
    public override val questionnaireType: String? = null,
    @param:JsonProperty("purpose")
    @get:JsonProperty("purpose")
    public override val purpose: String? = null,
    @param:JsonProperty("questionGroups")
    @get:JsonProperty("questionGroups")
    @get:NotNull
    @get:Valid
    public val questionGroups: List<QuestionGroup>,
) : AbstractQuestionnaire(questionnaireId, title, description, questionnaireType, purpose) {
    @get:JsonProperty("__type")
    @get:NotNull
    public override val type: String = "Questionnaire"
}
