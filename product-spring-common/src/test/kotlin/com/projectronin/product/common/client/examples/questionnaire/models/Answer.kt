package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "inputType",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = MultipleChoiceAnswer::class,
        name =
        "MULTIPLE_CHOICE_SELECT"
    ),
    JsonSubTypes.Type(
        value = SingleChoiceAnswer::class,
        name =
        "SINGLE_CHOICE_SELECT"
    ),
    JsonSubTypes.Type(
        value = NumericRangeAnswer::class,
        name =
        "INTEGER_RANGE"
    ),
    JsonSubTypes.Type(value = FreeTextAnswer::class, name = "FREE_TEXT")
)
public sealed class Answer(
    public open val questionGroupId: QuestionGroupIdentifier,
    public open val questionId: QuestionIdentifier,
) {
    public abstract val inputType: QuestionInputType
}

public data class MultipleChoiceAnswer(
    @param:JsonProperty("questionGroupId")
    @get:JsonProperty("questionGroupId")
    @get:NotNull
    @get:Valid
    public override val questionGroupId: QuestionGroupIdentifier,
    @param:JsonProperty("questionId")
    @get:JsonProperty("questionId")
    @get:NotNull
    @get:Valid
    public override val questionId: QuestionIdentifier,
    @param:JsonProperty("choices")
    @get:JsonProperty("choices")
    @get:NotNull
    @get:Valid
    public val choices: List<AnswerDefinitionIdentifier>,
) : Answer(questionGroupId, questionId) {
    @get:JsonProperty("inputType")
    @get:NotNull
    public override val inputType: QuestionInputType = QuestionInputType.MULTIPLE_CHOICE_SELECT
}

public data class SingleChoiceAnswer(
    @param:JsonProperty("questionGroupId")
    @get:JsonProperty("questionGroupId")
    @get:NotNull
    @get:Valid
    public override val questionGroupId: QuestionGroupIdentifier,
    @param:JsonProperty("questionId")
    @get:JsonProperty("questionId")
    @get:NotNull
    @get:Valid
    public override val questionId: QuestionIdentifier,
    @param:JsonProperty("choice")
    @get:JsonProperty("choice")
    @get:NotNull
    @get:Valid
    public val choice: AnswerDefinitionIdentifier,
) : Answer(questionGroupId, questionId) {
    @get:JsonProperty("inputType")
    @get:NotNull
    public override val inputType: QuestionInputType = QuestionInputType.SINGLE_CHOICE_SELECT
}

public data class NumericRangeAnswer(
    @param:JsonProperty("questionGroupId")
    @get:JsonProperty("questionGroupId")
    @get:NotNull
    @get:Valid
    public override val questionGroupId: QuestionGroupIdentifier,
    @param:JsonProperty("questionId")
    @get:JsonProperty("questionId")
    @get:NotNull
    @get:Valid
    public override val questionId: QuestionIdentifier,
    @param:JsonProperty("enteredValue")
    @get:JsonProperty("enteredValue")
    @get:NotNull
    public val enteredValue: Int,
) : Answer(questionGroupId, questionId) {
    @get:JsonProperty("inputType")
    @get:NotNull
    public override val inputType: QuestionInputType = QuestionInputType.INTEGER_RANGE
}

public data class FreeTextAnswer(
    @param:JsonProperty("questionGroupId")
    @get:JsonProperty("questionGroupId")
    @get:NotNull
    @get:Valid
    public override val questionGroupId: QuestionGroupIdentifier,
    @param:JsonProperty("questionId")
    @get:JsonProperty("questionId")
    @get:NotNull
    @get:Valid
    public override val questionId: QuestionIdentifier,
    @param:JsonProperty("enteredText")
    @get:JsonProperty("enteredText")
    @get:NotNull
    public val enteredText: String,
) : Answer(questionGroupId, questionId) {
    @get:JsonProperty("inputType")
    @get:NotNull
    public override val inputType: QuestionInputType = QuestionInputType.FREE_TEXT
}
