package com.example.smartplay

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val questionId = intent.getIntExtra("question_id", -1)
        val questionTitle = intent.getStringExtra("question_title") ?: ""
        val answers = intent.getStringArrayExtra("answers") ?: arrayOf()

        // Create actions for each answer option
        val actions = answers.mapIndexed { index, answer ->
            NotificationCompat.Action.Builder(
                R.drawable.ic_answer, answer, createResponsePendingIntent(context, questionId, index)
            ).build()
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, "question_channel")
            .setSmallIcon(R.drawable.ic_question)
            .setContentTitle(questionTitle)
            .setContentText("Please provide your response")
            .setActions(*actions.toTypedArray())
            .build()

        // Notify
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(questionId, notification)
    }

    private fun createResponsePendingIntent(context: Context, questionId: Int, answerIndex: Int): PendingIntent {
        val responseIntent = Intent(context, ResponseReceiver::class.java).apply {
            putExtra("question_id", questionId)
            putExtra("answer_index", answerIndex)
        }
        return PendingIntent.getBroadcast(
            context, questionId * 10 + answerIndex, responseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
