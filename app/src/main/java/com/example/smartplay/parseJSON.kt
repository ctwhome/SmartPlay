package com.example.smartplay

import com.google.gson.Gson

// Data classes
data class Question(
    val question_id: Int,
    val question_title: String,
    val answers: List<String>,
    val time_after_start_in_minutes: Int,
    val frequency_in_minutes: Int?
)

data class Workflow(
    val workflow_name: String,
    val questions: List<Question>
)

// Sample JSON parsing, get the JSON from the device or server
val jsonString =  // Your JSON string here

val workflows: List<Workflow> = Gson().fromJson(jsonString, object : TypeToken<List<Workflow>>() {}.type)
