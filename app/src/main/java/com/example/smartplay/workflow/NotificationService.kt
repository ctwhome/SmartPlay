package com.example.smartplay.workflow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartplay.R
import com.example.smartplay.SettingsActivity

class NotificationService : Service() {
    companion object {
        const val CHANNEL_ID = "SmartPlayChannel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_QUESTION_ID = "EXTRA_QUESTION_ID"
        const val EXTRA_QUESTION_TITLE = "EXTRA_QUESTION_TITLE"
        const val EXTRA_ANSWERS = "EXTRA_ANSWERS"
        private const val TAG = "NotificationService"
    }

    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationService created")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called")
        if (intent != null) {
            val questionId = intent.getIntExtra(EXTRA_QUESTION_ID, -1)
            val questionTitle = intent.getStringExtra(EXTRA_QUESTION_TITLE) ?: "SmartPlay Question"
            val answers = intent.getStringArrayExtra(EXTRA_ANSWERS) ?: arrayOf()

            if (questionId != -1) {
                Log.d(TAG, "Creating notification for question: $questionId")
                val notification = createNotification(questionId, questionTitle, answers)
                startForeground(NOTIFICATION_ID, notification)
                Log.d(TAG, "Notification posted for question: $questionId")
            } else {
                Log.e(TAG, "Invalid question ID received")
            }
        } else {
            Log.e(TAG, "Null intent received")
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(
                                    CHANNEL_ID,
                                    "SmartPlay Notifications",
                                    NotificationManager.IMPORTANCE_HIGH
                            )
                            .apply {
                                description = "Notifications for SmartPlay questions"
                                setShowBadge(true)
                            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun createNotification(
            questionId: Int,
            questionTitle: String,
            answers: Array<String>
    ): Notification {
        Log.d(TAG, "Creating notification for question: $questionId, title: $questionTitle")
        val intent =
                Intent(this, SettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(EXTRA_QUESTION_ID, questionId)
                }

        val pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val notificationBuilder =
                NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("SmartPlay Question")
                        .setContentText(questionTitle)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setOngoing(true)
                        .extend(
                                NotificationCompat.WearableExtender()
                                        .setHintShowBackgroundOnly(true)
                        )

        // Add actions for each answer
        answers.forEachIndexed { index, answer ->
            val actionIntent =
                    Intent(this, NotificationActionReceiver::class.java).apply {
                        action = "com.example.smartplay.ANSWER_$index"
                        putExtra(EXTRA_QUESTION_ID, questionId)
                        putExtra(EXTRA_QUESTION_TITLE, questionTitle)
                        putExtra("ANSWER", answer)
                    }
            val actionPendingIntent =
                    PendingIntent.getBroadcast(
                            this,
                            index,
                            actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
            notificationBuilder.addAction(NotificationCompat.Action(0, answer, actionPendingIntent))
        }

        return notificationBuilder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NotificationService destroyed")
    }
}
