package com.example.smartplay.workflow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
  companion object {
    private const val TAG = "NotificationReceiver"
  }

  override fun onReceive(context: Context, intent: Intent) {
    Log.d(TAG, "Received broadcast for notification")

    val questionId = intent.getIntExtra(NotificationService.EXTRA_QUESTION_ID, -1)
    val questionTitle = intent.getStringExtra(NotificationService.EXTRA_QUESTION_TITLE)
    val answers = intent.getStringArrayExtra(NotificationService.EXTRA_ANSWERS)

    if (questionId != -1 && questionTitle != null && answers != null) {
      val notificationIntent =
              Intent(context, NotificationService::class.java).apply {
                putExtra(NotificationService.EXTRA_QUESTION_ID, questionId)
                putExtra(NotificationService.EXTRA_QUESTION_TITLE, questionTitle)
                putExtra(NotificationService.EXTRA_ANSWERS, answers)
              }
      context.startService(notificationIntent)
      Log.d(TAG, "Started NotificationService for question: $questionId")
    } else {
      Log.e(TAG, "Invalid intent extras received")
    }
  }
}
