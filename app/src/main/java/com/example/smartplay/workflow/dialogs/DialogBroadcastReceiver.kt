package com.example.smartplay.workflow.dialogs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartplay.MyApplication
import com.example.smartplay.RecordingActivity
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
        val isAppInForeground = application.isAppInForeground
        val currentActivity = application.currentActivity

        if (isAppInForeground && currentActivity is RecordingActivity) {
            currentActivity.runOnUiThread {
                val dataRecorder = currentActivity.getDataRecorder()
                val dialogManager = DialogManager(currentActivity, dataRecorder)
                dialogManager.showCustomDialog(question)
            }
        } else {
            Log.d(TAG, "App is not in foreground or current activity is not RecordingActivity. Showing notification.")
            // Show a notification
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showNotification(
                question.question_id,
                question.question_title,
                question.answers
            )
        }
    }
}