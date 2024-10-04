package com.example.smartplay.recording

interface QuestionRecorder {
    fun writeQuestionsToCSV(
        timestamp: Long,
        questionId: String,
        questionTitle: String,
        answer: String
    )
}