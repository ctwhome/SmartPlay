
package com.example.smartplay.utils

interface QuestionRecorder {
    fun writeQuestionsToCSV(
        timestamp: Long,
        questionId: String,
        questionTitle: String,
        answer: String,
        state: String
    )
}