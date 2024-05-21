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

// List to keep track of handlers and runnables
val handlerRunnablePairs = mutableListOf<Pair<Handler, Runnable>>()

fun scheduleCustomDialogs(workflow: List<Workflow>, context: Context) {
    workflow[0].questions.forEach { question ->
//        Log.d(TAG, "Question: ${question.question_title}")
//        Log.d(TAG, "Answers: ${question.answers}")
//        Log.d(TAG, "Time after start in minutes: ${question.time_after_start_in_minutes}")
//        Log.d(TAG, "Frequency: ${question.frequency}")
//        Log.d(TAG, "Time between repetitions in minutes: ${question.time_between_repetitions_in_minutes}")
//        Log.d(TAG, "Scheduling initial dialog with delay: $initialDelay ms")
//        Log.d(TAG, "QUESTION: $question ms")

        // Schedule initial dialog if time_after_start_in_minutes is not null
        val startTime = question.time_after_start_in_minutes
        val initialDelay = startTime * 1000L // Convert to milliseconds
        val handler = Handler(Looper.getMainLooper())

        // Define the runnable
        val initialRunnable = Runnable {
            showMessageDialog(context, question)
            scheduleRepetitions(handler, question, context)
        }

        // Schedule the initial dialog
        handler.postDelayed(initialRunnable, initialDelay)

        // Add to list of handlers and runnables
        handlerRunnablePairs.add(handler to initialRunnable)
    }
}

fun scheduleRepetitions(handler: Handler, question: Question, context: Context) {
    val frequency = question.frequency
    val repetitionInterval = question.time_between_repetitions_in_minutes * 1000L // Convert to milliseconds

    Log.d(TAG, "Scheduling repetitions every: $repetitionInterval ms for $frequency times")

    for (i in 1 until frequency) {
        val delay = i * repetitionInterval

        // Define the runnable
        val repetitionRunnable = Runnable {
            showMessageDialog(context, question)
        }

        // Schedule the repetition
        handler.postDelayed(repetitionRunnable, delay)

        // Add to list of handlers and runnables
        handlerRunnablePairs.add(handler to repetitionRunnable)

        Log.d(TAG, "Scheduled repetition $i with delay: $delay ms")
    }
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

    // Save when the question was asked
    // "timestamp,question,asked/answer"



    // show dialog
    if (question.answers.isEmpty()) {
        Log.d(TAG, "No answers provided to the dialog")
        return
    }

    Log.d(TAG, "Showing dialog for question: ${question.question_title} with answers: $question.answers")

    val builder = AlertDialog.Builder(context)
    builder.setTitle(question.question_title)
    builder.setSingleChoiceItems(question.answers.toTypedArray(), -1) { dialog, index ->

        // Save the response to the CSV file
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
//    builder.setPositiveButton("OK") { dialog, _ ->
//        dialog.dismiss()
//    }
//    builder.setNegativeButton("Cancel") { dialog, _ ->
//        dialog.dismiss()
//    }

    val dialog: AlertDialog = builder.create()
    dialog.show()
}

/* Audio and Vibration */
fun playSound(context: Context) {
    // Play a notification sound
    val mediaPlayer = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
    mediaPlayer.setOnCompletionListener { mp ->
        mp.release()
    }
    mediaPlayer.start()
}

fun vibrate(context: Context) {
    // Vibrate the device
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
    vibrator.vibrate(vibrationEffect)
}

// Function to stop all scheduled notifications
fun stopAllNotifications() {
    Log.d(TAG, "Stopping all scheduled notifications...")
    for ((handler, runnable) in handlerRunnablePairs) {
        handler.removeCallbacks(runnable)
    }
    handlerRunnablePairs.clear()
    Log.d(TAG, "All scheduled notifications have been stopped.")
}
