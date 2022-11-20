package com.projectronin.product.common.client.examples

// import com.projectronin.product.common.client.AbstractServiceClient
// import com.projectronin.services.questionnaire.api.v1.models.Answer
// import com.projectronin.services.questionnaire.api.v1.models.AnswerDefinitionIdentifier
// import com.projectronin.services.questionnaire.api.v1.models.AnswerSubmission
// import com.projectronin.services.questionnaire.api.v1.models.AssignmentRequestContext
// import com.projectronin.services.questionnaire.api.v1.models.MultipleChoiceAnswer
// import com.projectronin.services.questionnaire.api.v1.models.QuestionGroupIdentifier
// import com.projectronin.services.questionnaire.api.v1.models.QuestionIdentifier
// import com.projectronin.services.questionnaire.api.v1.models.QuestionnaireAssignmentResponse
// import com.projectronin.services.questionnaire.api.v1.models.QuestionnaireAssignmentStateResponse
// import com.projectronin.product.common.client.auth.AuthBroker
// import com.projectronin.product.common.client.auth.PassThruAuthBroker
// import com.projectronin.product.common.client.exception.ServiceClientException
// import okhttp3.OkHttpClient
// import org.junit.jupiter.api.Disabled
// import org.junit.jupiter.api.Test
// import org.springframework.http.HttpHeaders
// import java.util.UUID

private const val QUESTIONNAIRE_URL = "http://localhost:8080"
// NOTE: Questionnaire Service requires a user token with a "udp_id"....which is difficult to actually come by
private const val AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2NTMzMjgzMjAsImlzcyI6IlNla2kiLCJqdGkiOiIycm9zcW05M2VlbmFwYmlrZm8wMXFrODEiLCJzdWIiOiIxNTFhMjUwOS1lNjllLTQwNDMtYmJhOC1kYmY5ODhkZGE1NTUiLCJ0ZW5hbnRpZCI6ImFwcG9zbmQifQ.gmX_Ad6sgTTW0iogI4kwuhYYbnpn5HGIE5RZxi56Ojs"

// TODO - remove this class before actual checkin
class QuestionnaireDemoTest {

    // Example of a client implemenation of Questionnaire Service (below)
    //   Plus some example test code of how to use it
    //     NOTE: code is commented out because it would rely on classes that are not in this project
    //       namely the Questionnaire model objects
    /*
    @Disabled
    @Test
    fun executeQuestionnaireDemo() {
        // create new Questionnaire Client
        val questionnaireClient = QuestionnaireClient(QUESTIONNAIRE_URL, PassThruAuthBroker(AUTH_TOKEN))

        // call create questionnaire
        val respA: QuestionnaireAssignmentResponse = questionnaireClient.createAssignQuestionnaire(generateTestQuestionnaire())
        println("ResponseA: $respA")

        val questionnaireAssignmentId: UUID = respA.data.questionnaireAssignmentId
        try {
            // cdll the other endpoint.   which will cause an exception because passing in non-sense  (most likely)
            val answerSubmission = generateTestAnswerSubmission()
            questionnaireClient.submitAnswers(questionnaireAssignmentId, answerSubmission)
        } catch (e: ServiceClientException) {
            e.printStackTrace()
        }

        val respB: QuestionnaireAssignmentStateResponse = questionnaireClient.getQuestionnaireState(questionnaireAssignmentId)
        println("ResponseB: $respB")
    }

    // Create a Test payload
    private fun generateTestQuestionnaire(): AssignmentRequestContext {
        return AssignmentRequestContext(
            mapOf(
                "PRIMARY_CANCER_TYPE" to listOf("GU")
            )
        )
    }

    // Create a Test payload... (no idea what any of this actually means)
    private fun generateTestAnswerSubmission(): AnswerSubmission {
        val choice = AnswerDefinitionIdentifier("da528f19-f639-4d6c-858d-aca816f596a3", "d50f51370ddfa898567c06db18aa9303a9857f401555173e6ccf2dbd4218396d")
        val qGrpId = QuestionGroupIdentifier("3fa85f64-5717-4562-b3fc-2c963f66afa6", "38a5eb55b61d8258c56e035275876b0fb543a972d112e99ca85")
        val qId = QuestionIdentifier("3fa85f64-5717-4562-b3fc-2c963f66afa6", "730d0d1ad76ee42b92f7997cc288fc2bc9172aaa")
        val answer: Answer = MultipleChoiceAnswer(qGrpId, qId, listOf(choice))
        return AnswerSubmission(true, listOf(answer))
    }
    */
}

// /////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////
// Below is how to implement a Questionnaire Client to be used for REST calls
//     This is for demo purposes and would ultimately find a 'better home'
//

/*
private const val QUESTIONNAIRE_PATH = "api/v1/questionnaire"
class QuestionnaireClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient()
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "QuestionnaireClient/1.0.0"
    }

    fun createAssignQuestionnaire(assignmentRequestContext: AssignmentRequestContext): QuestionnaireAssignmentResponse {
        return executePost("$baseUrl$QUESTIONNAIRE_PATH", assignmentRequestContext)
    }

    fun getQuestionnaireState(assignmentId: UUID): QuestionnaireAssignmentStateResponse {
        return executeGet("$baseUrl$QUESTIONNAIRE_PATH/$assignmentId")
    }

    fun submitAnswers(assignmentId: UUID, answerSubmission: AnswerSubmission) {
        executeRawPost("$baseUrl$QUESTIONNAIRE_PATH/$assignmentId", answerSubmission)
    }

    override fun getRequestHeaderMap(method: String, requestUrl: String, bearerAuthToken: String): MutableMap<String, String> {
        return super.getRequestHeaderMap(method, requestUrl, bearerAuthToken).apply {
            if (method == "POST" && requestUrl.contains("/questionnaire/")) {
                put(HttpHeaders.IF_MATCH, "true") // don't know what this is for, so just set to 'true'
            }
        }
    }
}
*/
