/* This workflow uses the previous custom dialog for the *//*

package com.example.smartplay.utils

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.smartplay.R
import com.example.smartplay.RecordingActivity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat



data class Question(
    val question_id: Int,
    val question_title: String,
    val answers: List<String>,
    val frequency: Int = 1,
    val time_after_start_in_minutes: Int = 1,
    val time_between_repetitions_in_minutes: Int = 1
)

data class Workflow(
    val workflow_name: String,
    val questions: List<Question>
)

val TAG = "CustomDialogScheduler"

// Map to keep track of handlers and runnables by question ID
val handlerRunnableMap = mutableMapOf<Int, MutableList<Pair<Handler, Runnable>>>()

// Map to keep track of active dialogs by question ID
val activeDialogsMap = mutableMapOf<Int, AlertDialog>()

fun scheduleCustomDialogs(workflow: List<Workflow>, context: Context) {
    workflow[0].questions.forEach { question ->
        val startTime = question.time_after_start_in_minutes
        val initialDelay = startTime * 1000L // Convert to milliseconds
        val handler = Handler(Looper.getMainLooper())

        // Define the runnable
        val initialRunnable = Runnable {
            showMessageDialog(context, question)
            scheduleRepetitions(handler, question, context)
        }

        // Cancel any existing tasks and close any existing dialogs with the same question ID
        cancelScheduledTasks(question.question_id)
        closeActiveDialog(question.question_id)

        // Schedule the initial dialog
        handler.postDelayed(initialRunnable, initialDelay)

        // Add to map of handlers and runnables by question ID
        handlerRunnableMap.computeIfAbsent(question.question_id) { mutableListOf() }.add(handler to initialRunnable)
    }
}

fun scheduleRepetitions(handler: Handler, question: Question, context: Context) {
    val frequency = question.frequency
    val repetitionInterval = question.time_between_repetitions_in_minutes * 1000L // Convert to milliseconds

    for (i in 1 until frequency) {
        val delay = i * repetitionInterval

        // Define the runnable
        val repetitionRunnable = Runnable {
            showMessageDialog(context, question)
        }

        // Schedule the repetition
        handler.postDelayed(repetitionRunnable, delay)

        // Add to map of handlers and runnables by question ID
        handlerRunnableMap.computeIfAbsent(question.question_id) { mutableListOf() }.add(handler to repetitionRunnable)
    }
}

fun cancelScheduledTasks(questionId: Int) {
    handlerRunnableMap[questionId]?.forEach { (handler, runnable) ->
        handler.removeCallbacks(runnable)
    }
    handlerRunnableMap.remove(questionId)
    Log.d(TAG, "Scheduled tasks for questionId $questionId have been canceled.")
}

fun closeActiveDialog(questionId: Int) {
    activeDialogsMap[questionId]?.let { dialog ->
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
    activeDialogsMap.remove(questionId)
    Log.d(TAG, "Active dialog for questionId $questionId has been closed.")
}


fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Question Channel"
        val descriptionText = "Channel for Question Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("QUESTION_CHANNEL_ID", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

*/
/* Notifications *//*

fun showMessageDialog(context: Context, question: Question) {
    // Create the notification channel
    createNotificationChannel(context)

    // Make sound and vibrate
    val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    val checkBoxVibration = sharedPref.getString("checkBoxVibration", "true")
    if (checkBoxVibration.toBoolean()) {
        vibrate(context)
    }
    val checkBoxSound = sharedPref.getString("checkBoxSound", "true")
    if (checkBoxSound.toBoolean()) {
        playSound(context)
    }

    val timestamp = System.currentTimeMillis()
    RecordingActivity.writeQuestionsToCSV(
        timestamp,
        question.question_id.toString(),
        question.question_title,
        "asked"
    )

    // Prepare notification
    val notificationId = question.question_id

    // Full-screen intent
    val fullScreenIntent = Intent(context, RecordingActivity::class.java).apply {
        putExtra("question_id", question.question_id)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val fullScreenPendingIntent = PendingIntent.getActivity(context, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notificationBuilder = NotificationCompat.Builder(context, "QUESTION_CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(question.question_title)
        .setContentText("Please answer the question.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .setFullScreenIntent(fullScreenPendingIntent, true)
        .setAutoCancel(true)

    // Add action buttons for each answer
    question.answers.forEachIndexed { index, answer ->
        val intent = Intent(context, RecordingActivity::class.java).apply {
            putExtra("question_id", question.question_id)
            putExtra("answer", answer)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, index, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder.addAction(
            R.drawable.ic_launcher_foreground,
            answer,
            pendingIntent
        )
    }

    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, notificationBuilder.build())
    }
}



*/
/* Audio and Vibration *//*

fun playSound(context: Context) {
    try {
        Log.d(TAG, "Attempting to play fallback sound")
        val mediaPlayer = MediaPlayer.create(context, R.raw.fallback_sound) // Ensure fallback_sound is a valid sound file in res/raw
        if (mediaPlayer == null) {
            Log.e(TAG, "Fallback MediaPlayer creation failed")
            return
        }
        mediaPlayer.setOnCompletionListener { mp ->
            Log.d(TAG, "Fallback sound playback completed")
            mp.release()
        }
        mediaPlayer.start()
        Log.d(TAG, "Fallback sound playback started")
    } catch (e: Exception) {
        Log.e("playFallbackSound", "Error playing fallback sound: ${e.message}")
    }
}

fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
    vibrator.vibrate(vibrationEffect)
}

// Function to stop all scheduled notifications
fun stopAllNotifications() {
    Log.d(TAG, "Stopping all scheduled notifications...")
    handlerRunnableMap.values.flatten().forEach { (handler, runnable) ->
        handler.removeCallbacks(runnable)
    }
    handlerRunnableMap.clear()

    activeDialogsMap.values.forEach { dialog ->
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
    activeDialogsMap.clear()
    Log.d(TAG, "All scheduled notifications and dialogs have been stopped.")
}
*/
