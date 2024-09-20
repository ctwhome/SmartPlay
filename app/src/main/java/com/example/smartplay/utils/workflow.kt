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

data class Question(
        val question_id: Int,
        val question_title: String,
        val answers: List<String>,
        val frequency: Int = 1,
        val time_after_start_in_minutes: Int = 1,
        val time_between_repetitions_in_minutes: Int = 1
)

data class Workflow(val workflow_name: String, val questions: List<Question>)

val TAG = "CustomDialogScheduler"

// Map to keep track of handlers and runnables by question ID
val handlerRunnableMap = mutableMapOf<Int, MutableList<Pair<Handler, Runnable>>>()

// Map to keep track of active dialogs by question ID
val activeDialogsMap = mutableMapOf<Int, AlertDialog>()

fun scheduleCustomDialogs(
        workflow: List<Workflow>,
        context: Context,
        questionRecorder: QuestionRecorder
) {
    Log.d(TAG, "Scheduling custom dialogs for workflow: ${workflow[0].workflow_name}")
    workflow[0].questions.forEach { question ->
        val startTime = question.time_after_start_in_minutes
        val initialDelay = startTime * 1000L // Now treated as seconds
        val handler = Handler(Looper.getMainLooper())

        Log.d(
                TAG,
                "Scheduling question ${question.question_id}: '${question.question_title}' with initial delay: $initialDelay ms"
        )

        // Define the runnable
        val initialRunnable = Runnable {
            Log.d(TAG, "Executing initial runnable for question ${question.question_id}")
            showMessageDialog(context, question, questionRecorder)
            scheduleRepetitions(handler, question, context, questionRecorder)
        }

        // Cancel any existing tasks and close any existing dialogs with the same question ID
        cancelScheduledTasks(question.question_id)
        closeActiveDialog(question.question_id)

        // Schedule the initial dialog
        handler.postDelayed(initialRunnable, initialDelay)

        // Add to map of handlers and runnables by question ID
        handlerRunnableMap
                .computeIfAbsent(question.question_id) { mutableListOf() }
                .add(handler to initialRunnable)
    }
    Log.d(TAG, "Finished scheduling all questions for workflow: ${workflow[0].workflow_name}")
}

fun scheduleRepetitions(
        handler: Handler,
        question: Question,
        context: Context,
        questionRecorder: QuestionRecorder
) {
    val frequency = question.frequency
    // Changed from minutes to seconds for testing purposes
    val repetitionInterval =
            question.time_between_repetitions_in_minutes * 1000L // Now treated as seconds

    Log.d(
            TAG,
            "Scheduling repetitions for question ${question.question_id}: frequency=$frequency, interval=$repetitionInterval ms"
    )

    for (i in 1 until frequency) {
        val delay = i * repetitionInterval

        // Define the runnable
        val repetitionRunnable = Runnable {
            Log.d(TAG, "Executing repetition $i for question ${question.question_id}")
            showMessageDialog(context, question, questionRecorder)
        }

        // Schedule the repetition
        handler.postDelayed(repetitionRunnable, delay)

        Log.d(
                TAG,
                "Scheduled repetition $i for question ${question.question_id} with delay: $delay ms"
        )

        // Add to map of handlers and runnables by question ID
        handlerRunnableMap
                .computeIfAbsent(question.question_id) { mutableListOf() }
                .add(handler to repetitionRunnable)
    }
}

fun cancelScheduledTasks(questionId: Int) {
    Log.d(TAG, "Cancelling scheduled tasks for question $questionId")
    handlerRunnableMap[questionId]?.forEach { (handler, runnable) ->
        handler.removeCallbacks(runnable)
    }
    handlerRunnableMap.remove(questionId)
}

fun closeActiveDialog(questionId: Int) {
    Log.d(TAG, "Closing active dialog for question $questionId")
    activeDialogsMap[questionId]?.let { dialog ->
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
    activeDialogsMap.remove(questionId)
}

fun showMessageDialog(context: Context, question: Question, questionRecorder: QuestionRecorder) {
    Log.d(
            TAG,
            "Showing message dialog for question ${question.question_id}: '${question.question_title}'"
    )

    // Make sound and vibrate
    val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    val checkBoxVibration = sharedPref.getString("checkBoxVibration", "true")
    if (checkBoxVibration.toBoolean()) {
        Log.d(TAG, "Vibration enabled, vibrating...")
        vibrate(context)
    }
    val checkBoxSound = sharedPref.getString("checkBoxSound", "true")
    if (checkBoxSound.toBoolean()) {
        Log.d(TAG, "Sound enabled, playing sound...")
        playSound(context)
    }

    val timestamp = System.currentTimeMillis()
    questionRecorder.writeQuestionsToCSV(
            timestamp,
            question.question_id.toString(),
            question.question_title,
            "asked",
            "asked"
    )

    // show dialog
    if (question.answers.isEmpty()) {
        Log.d(TAG, "No answers provided to the dialog")
        return
    }

    Log.d(
            TAG,
            "Building AlertDialog for question: ${question.question_title} with answers: ${question.answers}"
    )

    val builder = AlertDialog.Builder(context)
    builder.setTitle(question.question_title)
    builder.setSingleChoiceItems(question.answers.toTypedArray(), -1) { dialog, index ->
        val timestamp = System.currentTimeMillis()
        Log.d(
                TAG,
                "Selected answer: ${timestamp}, ${question.question_id}, ${question.question_title}, ${question.answers[index]}"
        )
        questionRecorder.writeQuestionsToCSV(
                timestamp,
                question.question_id.toString(),
                question.question_title,
                question.answers[index],
                "answered"
        )

        // Dismiss the dialog
        dialog.dismiss()
    }

    val dialog: AlertDialog = builder.create()
    closeActiveDialog(question.question_id) // Close any existing dialog before showing the new one
    activeDialogsMap[question.question_id] = dialog
    dialog.show()
    Log.d(TAG, "AlertDialog shown for question ${question.question_id}")
}

/* Audio and Vibration */
fun playSound(context: Context) {
    try {
        Log.d(TAG, "Attempting to play fallback sound")
        val mediaPlayer =
                MediaPlayer.create(
                        context,
                        R.raw.fallback_sound
                ) // Ensure fallback_sound is a valid sound file in res/raw
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
    Log.d(TAG, "Vibration executed")
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
