package com.projectronin.services.questionnaire.api.v1.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "__type",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = QuestionnaireAssignmentState::class,
        name =
        "QuestionnaireAssignmentState"
    ),
    JsonSubTypes.Type(
        value = QuestionnaireAssignment::class,
        name =
        "QuestionnaireAssignment"
    )
)
public sealed class AbstractQuestionnaireAssignment(
    public open val questionnaireAssignmentId: UUID,
    public open val patientId: String,
    public open val assignmentDate: OffsetDateTime,
) {
    public abstract val type: String
}

public data class QuestionnaireAssignmentState(
    @param:JsonProperty("questionnaireAssignmentId")
    @get:JsonProperty("questionnaireAssignmentId")
    @get:NotNull
    public override val questionnaireAssignmentId: UUID,
    @param:JsonProperty("patientId")
    @get:JsonProperty("patientId")
    @get:NotNull
    @get:Size(
        min = 1,
        max = 255,
    )
    public override val patientId: String,
    @param:JsonProperty("assignmentDate")
    @get:JsonProperty("assignmentDate")
    @get:NotNull
    public override val assignmentDate: OffsetDateTime,
    @param:JsonProperty("questionnaire")
    @get:JsonProperty("questionnaire")
    @get:NotNull
    @get:Valid
    public val questionnaire: QuestionnaireState,
) : AbstractQuestionnaireAssignment(questionnaireAssignmentId, patientId, assignmentDate) {
    @get:JsonProperty("__type")
    @get:NotNull
    public override val type: String = "QuestionnaireAssignmentState"
}

public data class QuestionnaireAssignment(
    @param:JsonProperty("questionnaireAssignmentId")
    @get:JsonProperty("questionnaireAssignmentId")
    @get:NotNull
    public override val questionnaireAssignmentId: UUID,
    @param:JsonProperty("patientId")
    @get:JsonProperty("patientId")
    @get:NotNull
    @get:Size(
        min = 1,
        max = 255,
    )
    public override val patientId: String,
    @param:JsonProperty("assignmentDate")
    @get:JsonProperty("assignmentDate")
    @get:NotNull
    public override val assignmentDate: OffsetDateTime,
    @param:JsonProperty("questionnaire")
    @get:JsonProperty("questionnaire")
    @get:NotNull
    @get:Valid
    public val questionnaire: Questionnaire,
) : AbstractQuestionnaireAssignment(questionnaireAssignmentId, patientId, assignmentDate) {
    @get:JsonProperty("__type")
    @get:NotNull
    public override val type: String = "QuestionnaireAssignment"
}
