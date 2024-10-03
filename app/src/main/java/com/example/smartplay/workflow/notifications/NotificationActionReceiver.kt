package com.example.smartplay.workflow.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartplay.workflow.QuestionRecorder

class NotificationActionReceiver : BroadcastReceiver() {
    private val TAG = "NotificationActionReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        val questionId = intent?.getIntExtra("EXTRA_QUESTION_ID", -1) ?: -1
        val questionTitle = intent?.getStringExtra("EXTRA_QUESTION_TITLE") ?: ""
        val answer = intent?.getStringExtra("ANSWER") ?: ""

        if (questionId != -1 && context != null) {
            Log.d(TAG, "Received answer for question $questionId: $answer")

            // Record the answer
            val appContext = context.applicationContext
            if (appContext is QuestionRecorder) {
                val timestamp = System.currentTimeMillis()
                appContext.writeQuestionsToCSV(timestamp, questionId.toString(), questionTitle, answer)
                Log.d(TAG, "Answer recorded for question $questionId")
            } else {
                Log.e(TAG, "Application context does not implement QuestionRecorder")
            }
        } else {
            Log.e(TAG, "Invalid question ID or context is null")
        }
    }
}
