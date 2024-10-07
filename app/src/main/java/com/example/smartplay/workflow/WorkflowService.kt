package com.example.smartplay.workflow

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartplay.R

class WorkflowService : Service() {
    private val TAG = "WorkflowService"
    private lateinit var alarmManager: AlarmManager
    private val pendingIntents = mutableListOf<PendingIntent>()
    private val CHANNEL_ID = "WorkflowServiceChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WorkflowService created")
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Workflow Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with startId: $startId")
        val workflow = intent?.getSerializableExtra("workflow") as? Workflow
        if (workflow != null) {
            Log.d(TAG, "Received workflow with ${workflow.questions.size} questions")
            scheduleAlarms(workflow)
        } else {
            Log.e(TAG, "No workflow data received")
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, WorkflowService::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Workflow Service")
            .setContentText("Running workflow...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun scheduleAlarms(workflow: Workflow) {
        Log.d(TAG, "Scheduling alarms for workflow")
        workflow.questions.forEachIndexed { index, question ->
            Log.d(TAG, "Scheduling alarm for question ${index + 1}/${workflow.questions.size}")
            scheduleAlarmForQuestion(question)
        }
        Log.d(TAG, "All alarms scheduled")
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarmForQuestion(question: Question) {
        val triggerAtMillis = SystemClock.elapsedRealtime() + question.time_after_start * 1000L

        val intent = Intent(this, QuestionReceiver::class.java).apply {
            putExtra("question", question)
        }

        val pendingIntentId = question.question_id
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            pendingIntentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        pendingIntents.add(pendingIntent)
        Log.d(TAG, "Scheduled alarm for question ID: ${question.question_id}, trigger time: ${triggerAtMillis - SystemClock.elapsedRealtime()}ms from now")

        // Schedule repeated questions if frequency > 1
        if (question.frequency > 1) {
            for (i in 1 until question.frequency) {
                val repeatedTriggerAtMillis = triggerAtMillis + question.time_between_repetitions * 1000L * i
                val repeatedIntent = Intent(this, QuestionReceiver::class.java).apply {
                    putExtra("question", question)
                    putExtra("repetition", i)
                }
                val repeatedPendingIntentId = pendingIntentId + i * 1000  // Ensure unique IDs
                val repeatedPendingIntent = PendingIntent.getBroadcast(
                    this,
                    repeatedPendingIntentId,
                    repeatedIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    repeatedTriggerAtMillis,
                    repeatedPendingIntent
                )
                pendingIntents.add(repeatedPendingIntent)
                Log.d(TAG, "Scheduled repeated alarm for question ID: ${question.question_id}, repetition: $i, trigger time: ${repeatedTriggerAtMillis - SystemClock.elapsedRealtime()}ms from now")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel all scheduled alarms
        pendingIntents.forEach { pendingIntent ->
            alarmManager.cancel(pendingIntent)
        }
        pendingIntents.clear()
        Log.d(TAG, "WorkflowService destroyed, all alarms canceled")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
