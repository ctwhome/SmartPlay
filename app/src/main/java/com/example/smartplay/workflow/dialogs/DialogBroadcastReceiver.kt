package com.example.smartplay.workflow.dialogs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartplay.MyApplication
import com.example.smartplay.workflow.Question
import com.example.smartplay.workflow.notifications.NotificationHelper

class DialogBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "DialogBroadcastReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.smartplay.SHOW_DIALOG") {
            val question = intent.getSerializableExtra("question") as? Question
            if (question != null) {
                Log.d(TAG, "Received broadcast to show dialog for question: ${question.question_id}")
                showCustomDialog(context, question)
            } else {
                Log.e(TAG, "No question data in intent")
            }
        }
    }

    private fun showCustomDialog(context: Context, question: Question) {
        val application = context.applicationContext as MyApplication
        val currentActivity = application.currentActivity

        if (currentActivity != null) {
            currentActivity.runOnUiThread {
                val dialogManager = DialogManager(currentActivity) { q, answer ->
                    // Handle the answer here
                    Log.d(TAG, "Answer selected: $answer for question ${q.question_id}")
                    // You might want to send this answer to a service or repository
                    application.writeQuestionsToCSV(
                        System.currentTimeMillis(),
                        q.question_id.toString(),
                        q.question_title,
                        answer,
                        "answered"
                    )
                }
                dialogManager.showCustomDialog(question)
            }
        } else {
            Log.e(TAG, "No active activity to show dialog")
            // Fallback to showing a notification
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showNotification(
                question.question_id,
                question.question_title,
                question.answers
            )
        }
    }
}