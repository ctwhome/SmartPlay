package com.example.smartplay.workflow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smartplay.R
import com.example.smartplay.RecordingActivity

class NotificationHelper(private val context: Context) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "SmartPlayChannel"
        private const val CHANNEL_NAME = "SmartPlay Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for SmartPlay questions"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(questionId: Int, questionTitle: String, answers: List<String>) {
        val intent = Intent(context, RecordingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_QUESTION_ID", questionId)
            putExtra("EXTRA_QUESTION_TITLE", questionTitle)
            putExtra("EXTRA_ANSWERS", answers.toTypedArray())
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            questionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("SmartPlay Question")
            .setContentText(questionTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Add actions for each answer
        answers.forEachIndexed { index, answer ->
            val actionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = "com.example.smartplay.ANSWER_$index"
                putExtra("EXTRA_QUESTION_ID", questionId)
                putExtra("EXTRA_QUESTION_TITLE", questionTitle)
                putExtra("ANSWER", answer)
            }

            val actionPendingIntent = PendingIntent.getBroadcast(
                context,
                index,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            notificationBuilder.addAction(NotificationCompat.Action(0, answer, actionPendingIntent))
        }

        notificationManager.notify(questionId, notificationBuilder.build())
    }
}