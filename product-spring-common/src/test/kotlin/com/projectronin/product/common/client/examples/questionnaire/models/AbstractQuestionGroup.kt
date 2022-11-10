package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import javax.validation.Valid
import javax.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "__type",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = QuestionGroupState::class,
        name =
        "QuestionGroupState"
    ),
    JsonSubTypes.Type(value = QuestionGroup::class, name = "QuestionGroup")
)
public sealed class AbstractQuestionGroup(
    public open val questionGroupId: QuestionGroupIdentifier,
    public open val patientTitle: String? = null,
    public open val providerTitle: String? = null,
    public open val description: String? = null,
    public open val requiredTags: RequiredTags? = null,
) {
    public abstract val type: String
}

public data class QuestionGroupState(
    @param:JsonProperty("questionGroupId")
    @get:JsonProperty("questionGroupId")
    @get:NotNull
    @get:Valid
    public override val questionGroupId: QuestionGroupIdentifier,
    @param:JsonProperty("patientTitle")
    @get:JsonProperty("patientTitle")
    public override val patientTitle: String? = null,
    @param:JsonProperty("providerTitle")
    @get:JsonProperty("providerTitle")
    public override val providerTitle: String? = null,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    public override val description: String? = null,
    @param:JsonProperty("requiredTags")
    @get:JsonProperty("requiredTags")
    @get:Valid
    public override val requiredTags: RequiredTags? = null,
    @param:JsonProperty("questions")
    @get:JsonProperty("questions")
    @get:NotNull
    @get:Valid
    public val questions: List<QuestionState>,
) : AbstractQuestionGroup(questionGroupId, patientTitle, providerTitle, description, requiredTags) {
    @get:JsonProperty("__type")
    @get:NotNull
    public override val type: String = "QuestionGroupState"
}

public data class QuestionGroup(
    @param:JsonProperty("questionGroupId")
    @get:JsonProperty("questionGroupId")
    @get:NotNull
    @get:Valid
    public override val questionGroupId: QuestionGroupIdentifier,
    @param:JsonProperty("patientTitle")
    @get:JsonProperty("patientTitle")
    public override val patientTitle: String? = null,
    @param:JsonProperty("providerTitle")
    @get:JsonProperty("providerTitle")
    public override val providerTitle: String? = null,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    public override val description: String? = null,
    @param:JsonProperty("requiredTags")
    @get:JsonProperty("requiredTags")
    @get:Valid
    public override val requiredTags: RequiredTags? = null,
    @param:JsonProperty("questions")
    @get:JsonProperty("questions")
    @get:NotNull
    @get:Valid
    public val questions: List<Question>,
) : AbstractQuestionGroup(questionGroupId, patientTitle, providerTitle, description, requiredTags) {
    @get:JsonProperty("__type")
    @get:NotNull
    public override val type: String = "QuestionGroup"
}
