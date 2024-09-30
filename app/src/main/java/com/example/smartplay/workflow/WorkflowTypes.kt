package com.example.smartplay.workflow

data class Workflow(val workflow_name: String, val questions: List<Question>)

data class Question(
    val question_id: Int,
    val question_title: String,
    val answers: List<String>,
    val time_after_start: Int,
    val frequency: Int,
    val time_between_repetitions: Int
)