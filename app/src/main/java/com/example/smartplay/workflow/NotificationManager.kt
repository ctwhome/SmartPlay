package com.example.smartplay.workflow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartplay.R
import com.example.smartplay.RecordingActivity

class NotificationManager(private val context: Context) {
    private val TAG = "NotificationManager"
    private val channelId = "SmartPlayChannel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SmartPlay Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for SmartPlay questions"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(question: Question) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, RecordingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_QUESTION_ID", question.question_id)
            putExtra("EXTRA_QUESTION_TITLE", question.question_title)
            putExtra("EXTRA_ANSWERS", question.answers.toTypedArray())
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            question.question_id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("SmartPlay Question")
            .setContentText(question.question_title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Add actions for each answer
        question.answers.forEachIndexed { index, answer ->
            val actionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = "com.example.smartplay.ANSWER_$index"
                putExtra("EXTRA_QUESTION_ID", question.question_id)
                putExtra("EXTRA_QUESTION_TITLE", question.question_title)
                putExtra("ANSWER", answer)
            }

            val actionPendingIntent = PendingIntent.getBroadcast(
                context,
                question.question_id * 100 + index, // Unique request code
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            notificationBuilder.addAction(
                NotificationCompat.Action(
                    0,
                    answer,
                    actionPendingIntent
                )
            )
        }

        notificationManager.notify(question.question_id, notificationBuilder.build())
        Log.d(TAG, "Notification sent for question: ${question.question_id}")
    }
}