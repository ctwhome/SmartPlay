package com.example.smartplay.utils

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

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

fun scheduleCustomDialogs(workflow: List<Workflow>, context: Context) {
    workflow[0].questions.forEach { question ->
        Log.d(TAG, "Question: ${question.question_title}")
        Log.d(TAG, "Answers: ${question.answers}")
        Log.d(TAG, "Time after start in minutes: ${question.time_after_start_in_minutes}")
        Log.d(TAG, "Frequency: ${question.frequency}")
        Log.d(TAG, "Time between repetitions in minutes: ${question.time_between_repetitions_in_minutes}")

        // Schedule initial dialog if time_after_start_in_minutes is not null
        val startTime = question.time_after_start_in_minutes
//        val initialDelay = startTime * 60 * 1000L // Convert to milliseconds
        val initialDelay = startTime * 1000L // Convert to milliseconds
        val handler = Handler(Looper.getMainLooper())

        Log.d(TAG, "Scheduling initial dialog with delay: $initialDelay ms")

        // Schedule the initial dialog
        handler.postDelayed({
            showMessageDialog(context, question.question_title, question.answers)
            scheduleRepetitions(handler, question, context)
        }, initialDelay)
    }
}

fun scheduleRepetitions(handler: Handler, question: Question, context: Context) {
    val frequency = question.frequency
//    val repetitionInterval = question.time_between_repetitions_in_minutes * 60 * 1000L // Convert to milliseconds
    val repetitionInterval = question.time_between_repetitions_in_minutes * 1000L // Convert to milliseconds

    Log.d(TAG, "Scheduling repetitions every: $repetitionInterval ms for $frequency times")

    for (i in 1 until frequency) {
        val delay = i * repetitionInterval
        handler.postDelayed({
            showMessageDialog(context, question.question_title, question.answers)
        }, delay)
        Log.d(TAG, "Scheduled repetition $i with delay: $delay ms")
    }
}

fun showMessageDialog(context: Context, question: String, answers: List<String>) {
    // Make sound and vibrate
    playSound(context)
    vibrate(context)


    // show dialog
    if (answers.isEmpty()) {
        Log.d(TAG, "No answers provided to the dialog")
        return
    }

    Log.d(TAG, "Showing dialog for question: $question with answers: $answers")

    val builder = AlertDialog.Builder(context)
    builder.setTitle(question)
    builder.setSingleChoiceItems(answers.toTypedArray(), -1) { dialog, index ->
        Log.d(TAG, "Selected answer: ${answers[index]}")
        dialog.dismiss()
    }
    builder.setPositiveButton("OK") { dialog, _ ->
        dialog.dismiss()
    }
    builder.setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
    }

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