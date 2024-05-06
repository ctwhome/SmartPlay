package com.example.smartplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ResponseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val questionId = intent.getIntExtra("question_id", -1)
        val answerIndex = intent.getIntExtra("answer_index", -1)

        // Record this data into a CSV or other storage solution
        // Example: Save to Shared Preferences (replace this with your desired storage)
        val preferences = context.getSharedPreferences("responses", Context.MODE_PRIVATE)
        preferences.edit().putString("response_$questionId", "Answer Index: $answerIndex").apply()
    }
}
