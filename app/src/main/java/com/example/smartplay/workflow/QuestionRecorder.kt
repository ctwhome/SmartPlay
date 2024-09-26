
package com.example.smartplay.workflow

interface QuestionRecorder {
    fun writeQuestionsToCSV(
        timestamp: Long,
        questionId: String,
        questionTitle: String,
        answer: String,
        state: String
    )
}