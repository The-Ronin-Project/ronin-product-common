package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "actionType",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = JumpToQuestionAction::class,
        name =
        "JUMP_TO_QUESTION"
    ),
    JsonSubTypes.Type(
        value = NextQuestionAction::class,
        name =
        "NEXT_QUESTION"
    ),
    JsonSubTypes.Type(
        value = EndQuestionGroupAction::class,
        name =
        "END_QUESTION_GROUP"
    )
)
public sealed class Action() {
    public abstract val actionType: ActionType
}

public data class JumpToQuestionAction(
    @param:JsonProperty("questionGroupId")
    @get:JsonProperty("questionGroupId")
    @get:NotNull
    @get:Valid
    public val questionGroupId: QuestionGroupIdentifier,
    @param:JsonProperty("questionId")
    @get:JsonProperty("questionId")
    @get:NotNull
    @get:Valid
    public val questionId: QuestionIdentifier,
) : Action() {
    @get:JsonProperty("actionType")
    @get:NotNull
    public override val actionType: ActionType = ActionType.JUMP_TO_QUESTION
}

public class NextQuestionAction() : Action() {
    @get:JsonProperty("actionType")
    @get:NotNull
    public override val actionType: ActionType = ActionType.NEXT_QUESTION
}

public class EndQuestionGroupAction() : Action() {
    @get:JsonProperty("actionType")
    @get:NotNull
    public override val actionType: ActionType = ActionType.END_QUESTION_GROUP
}
