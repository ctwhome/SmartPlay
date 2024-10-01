// AlarmReceiver.kt
package com.example.smartplay.workflow

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartplay.workflow.notifications.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val question = intent.getSerializableExtra("question") as? Question
        if (question != null) {
            Log.d(TAG, "Alarm received for question: ${question.question_id}")

            if (isAppInForeground(context)) {
                // App is in foreground, send broadcast to show custom dialog
                val dialogIntent = Intent("com.example.smartplay.SHOW_DIALOG")
                dialogIntent.putExtra("question", question)
                context.sendBroadcast(dialogIntent)
            } else {
                // App is in background, show notification
                val notificationHelper = NotificationHelper(context)
                notificationHelper.showNotification(
                    question.question_id,
                    question.question_title,
                    question.answers
                )
            }
        } else {
            Log.e(TAG, "No question data in intent")
        }
    }

    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }
}
