/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.example

import com.projectronin.services.questionnaire.api.v1.controllers.QuestionnaireController
import com.projectronin.services.questionnaire.api.v1.models.AnswerDefinition

class App {
    val greeting: String
        get() {
            return "Hello World!"
        }

    var wrapper: AnswerDefinition? = null

    var controller: QuestionnaireController? = null
}

fun main() {
    println(App().greeting)
}