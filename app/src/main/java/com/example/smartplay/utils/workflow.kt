package com.example.smartplay.utils

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.smartplay.RecordingActivity

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
}

fun closeActiveDialog(questionId: Int) {
    activeDialogsMap[questionId]?.let { dialog ->
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
    activeDialogsMap.remove(questionId)
}

fun showMessageDialog(context: Context, question: Question) {
    // Make sound and vibrate
    playSound(context)
    vibrate(context)
    val timestamp = System.currentTimeMillis()
    RecordingActivity.writeQuestionsToCSV(
        timestamp,
        question.question_id.toString(),
        question.question_title,
        "asked"
    )

    // show dialog
    if (question.answers.isEmpty()) {
        Log.d(TAG, "No answers provided to the dialog")
        return
    }

    Log.d(TAG, "Showing dialog for question: ${question.question_title} with answers: ${question.answers}")

    val builder = AlertDialog.Builder(context)
    builder.setTitle(question.question_title)
    builder.setSingleChoiceItems(question.answers.toTypedArray(), -1) { dialog, index ->
        val timestamp = System.currentTimeMillis()
        Log.d(TAG, "Selected answer: ${timestamp}, ${question.question_id}, ${question.question_title}, ${question.answers[index]}")
        RecordingActivity.writeQuestionsToCSV(
            timestamp,
            question.question_id.toString(),
            question.question_title,
            question.answers[index]
        )

        // Dismiss the dialog
        dialog.dismiss()
    }

    val dialog: AlertDialog = builder.create()
    closeActiveDialog(question.question_id) // Close any existing dialog before showing the new one
    activeDialogsMap[question.question_id] = dialog
    dialog.show()
}

/* Audio and Vibration */
fun playSound(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
    mediaPlayer.setOnCompletionListener { mp ->
        mp.release()
    }
    mediaPlayer.start()
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
