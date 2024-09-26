package com.example.smartplay.workflow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smartplay.R

class NotificationService : Service() {
    private val CHANNEL_ID = "NotificationChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Returning START_STICKY makes sure the service is restarted if killed by the system
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Notification Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "This channel is used to show notifications."
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val goodIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "com.example.smartplay.GOOD"
        }
        val goodPendingIntent = PendingIntent.getBroadcast(this, 0, goodIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val badIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "com.example.smartplay.BAD"
        }
        val badPendingIntent = PendingIntent.getBroadcast(this, 0, badIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val fineIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "com.example.smartplay.FINE"
        }
        val finePendingIntent = PendingIntent.getBroadcast(this, 0, fineIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("How do you feel?")
            .setSmallIcon(R.drawable.ic_notification)
            .addAction(NotificationCompat.Action(0, "Good", goodPendingIntent))
            .addAction(NotificationCompat.Action(0, "Bad", badPendingIntent))
            .addAction(NotificationCompat.Action(0, "Fine", finePendingIntent))
            .setOngoing(true)
            .build()
    }
}
