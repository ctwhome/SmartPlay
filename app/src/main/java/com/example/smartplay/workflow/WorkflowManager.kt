package com.example.smartplay.workflow

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.smartplay.R
import com.example.smartplay.data.DataRecorder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference

class WorkflowManager(
        context: Context,
        private val dataRecorder: DataRecorder,
        private val sharedPreferences: SharedPreferences
) {
    private val TAG = "WorkflowManager"
    private lateinit var workflows: List<Workflow>
    private lateinit var selectedWorkflow: Workflow
    private lateinit var workflowContent: String
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val handler = Handler(Looper.getMainLooper())
    private val scheduledRunnables = mutableListOf<Runnable>()

    companion object {
        const val PREF_VIBRATION = "checkBoxVibration"
        const val PREF_SOUND = "checkBoxSound"
    }

    fun initializeWorkflow(workflowString: String, selectedWorkflowName: String): Workflow? {
        Log.d(TAG, "Initializing workflow. Selected workflow name: $selectedWorkflowName")
        val gson = Gson()
        val workflowListType = object : TypeToken<List<Workflow>>() {}.type

        return try {
            workflowContent = workflowString
            workflows = gson.fromJson(workflowString, workflowListType)
            Log.d(TAG, "Parsed workflows: ${workflows.size}")

            selectedWorkflow =
                    workflows.first { it.workflow_name.trim() == selectedWorkflowName.trim() }
            Log.d(TAG, "Selected Workflow: ${selectedWorkflow.workflow_name}")
            Log.d(
                    TAG,
                    "Number of questions in selected workflow: ${selectedWorkflow.questions.size}"
            )
            Log.d(TAG, "Questions: ${selectedWorkflow.questions}")
            selectedWorkflow
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing workflow JSON: ${e.message}", e)
            null
        }
    }

    fun scheduleCustomDialogs(workflow: Workflow) {
        Log.d(TAG, "Scheduling custom dialogs for workflow: ${workflow.workflow_name}")
        workflow.questions.forEachIndexed { index, question ->
            Log.d(TAG, "Scheduling dialog for question ${index + 1}: ${question.question_id}")
            scheduleDialog(question)
        }
    }

    private fun scheduleDialog(question: Question) {
        val delayMillis = question.time_after_start_in_minutes * 1000L
        // val delayMillis = question.time_after_start_in_minutes * 60 * 1000L   TODO: use this 60
        // to use minutes, after testing
        Log.d(
                TAG,
                "Scheduling dialog for question ${question.question_id} with delay: $delayMillis ms"
        )
        val runnable = Runnable {
            Log.d(TAG, "Executing runnable for question ${question.question_id}")
            showCustomDialog(question)
        }
        scheduledRunnables.add(runnable)
        handler.postDelayed(runnable, delayMillis)
        Log.d(TAG, "Dialog scheduled for question ${question.question_id}")

        // Schedule repeated questions if frequency > 1
        if (question.frequency > 1) {
            for (i in 1 until question.frequency) {
                val repeatedDelayMillis =
                        delayMillis +
                                (question.time_between_repetitions_in_minutes * 60 * 1000L * i)
                val repeatedRunnable = Runnable {
                    Log.d(
                            TAG,
                            "Executing repeated runnable for question ${question.question_id}, repetition ${i + 1}"
                    )
                    showCustomDialog(question)
                }
                scheduledRunnables.add(repeatedRunnable)
                handler.postDelayed(repeatedRunnable, repeatedDelayMillis)
                Log.d(
                        TAG,
                        "Repeated dialog scheduled for question ${question.question_id}, repetition ${i + 1} with delay: $repeatedDelayMillis ms"
                )
            }
        }
    }

    private fun showCustomDialog(question: Question) {
        val context =
                contextRef.get()
                        ?: run {
                            Log.e(TAG, "Context is null, cannot show dialog")
                            return
                        }
        Log.d(TAG, "Showing custom dialog for question: ${question.question_id}")

        // Make sound and vibrate
        val checkBoxVibration = sharedPreferences.getString(PREF_VIBRATION, "true")
        Log.d(TAG, "Vibration enabled: $checkBoxVibration")
        if (checkBoxVibration?.toBoolean() == true) {
            vibrate(context)
        }
        val checkBoxSound = sharedPreferences.getString(PREF_SOUND, "true")
        Log.d(TAG, "Sound enabled: $checkBoxSound")
        if (checkBoxSound?.toBoolean() == true) {
            playSound(context)
        }

        // Record that the question is being asked
        recordQuestionAsked(question)

        val builder = AlertDialog.Builder(context)
        builder.setTitle(question.question_title)

        val options = question.answers.toTypedArray()
        builder.setItems(options) { dialog, which ->
            val selectedOption = options[which]
            recordAnswer(question, selectedOption)
            dialog.dismiss()
        }
        builder.show()
        Log.d(TAG, "Custom dialog shown for question: ${question.question_id}")
    }

    private fun playSound(context: Context) {
        try {
            Log.d(TAG, "Attempting to play fallback sound")
            val mediaPlayer = MediaPlayer.create(context, R.raw.fallback_sound)
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
            Log.e(TAG, "Error playing fallback sound: ${e.message}")
        }
    }

    private fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
        Log.d(TAG, "Vibration executed")
    }

    private fun recordQuestionAsked(question: Question) {
        val timestamp = System.currentTimeMillis()
        Log.d(TAG, "Recording question asked: ${question.question_id} at timestamp: $timestamp")
        dataRecorder.writeQuestionData(
                timestamp,
                question.question_id.toString(),
                question.question_title,
                "ASKED",
                "asked"
        )
    }

    private fun recordAnswer(question: Question, answer: String) {
        val timestamp = System.currentTimeMillis()
        Log.d(TAG, "Recording answer for question ${question.question_id} at timestamp: $timestamp")
        dataRecorder.writeQuestionData(
                timestamp,
                question.question_id.toString(),
                question.question_title,
                answer,
                "answered"
        )
        Log.d(TAG, "Answer recorded: ${question.question_id}, $answer")
    }

    fun cancelScheduledDialogs() {
        Log.d(TAG, "Cancelling all scheduled dialogs")
        scheduledRunnables.forEach { handler.removeCallbacks(it) }
        scheduledRunnables.clear()
        Log.d(TAG, "All scheduled dialogs cancelled")
    }

    fun rescheduleDialogs() {
        Log.d(TAG, "Rescheduling dialogs")
        cancelScheduledDialogs()
        if (::selectedWorkflow.isInitialized) {
            scheduleCustomDialogs(selectedWorkflow)
            Log.d(TAG, "Dialogs rescheduled for workflow: ${selectedWorkflow.workflow_name}")
        } else {
            Log.e(TAG, "Cannot reschedule dialogs: selectedWorkflow is not initialized")
        }
    }
}

data class Workflow(val workflow_name: String, val questions: List<Question>)

data class Question(
        val question_id: Int,
        val question_title: String,
        val answers: List<String>,
        val time_after_start_in_minutes: Int,
        val frequency: Int,
        val time_between_repetitions_in_minutes: Int
)
