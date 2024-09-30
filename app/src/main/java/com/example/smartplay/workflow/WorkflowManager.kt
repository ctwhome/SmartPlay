package com.example.smartplay.workflow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.smartplay.MyApplication
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

    private val dialogManager: DialogManager
    private val notificationManager: NotificationManager
    private val soundAndVibrationManager: SoundAndVibrationManager
    private val workflowScheduler: WorkflowScheduler

    init {
        dialogManager = DialogManager(context) { question, answer -> recordAnswer(question, answer) }
        notificationManager = NotificationManager(context)
        soundAndVibrationManager = SoundAndVibrationManager(context)
        workflowScheduler = WorkflowScheduler { question -> showQuestion(question) }
        // start the foreground service
        ContextCompat.startForegroundService(context, Intent(context, NotificationService::class.java))
    }

    private fun showQuestion(question: Question) {
        val context = contextRef.get() ?: return
        Log.d(TAG, "Showing question: ${question.question_id}")

        // Make sound and vibrate
        val checkBoxVibration = sharedPreferences.getString(PREF_VIBRATION, "true")
        if (checkBoxVibration?.toBoolean() == true) {
            soundAndVibrationManager.vibrate()
        }
        val checkBoxSound = sharedPreferences.getString(PREF_SOUND, "true")
        if (checkBoxSound?.toBoolean() == true) {
            soundAndVibrationManager.playSound()
        }

        // Record that the question is being asked
        recordQuestionAsked(question)

        if (isAppInForeground(context)) {
            dialogManager.showCustomDialog(question)

            //CONTINUE HEREEEEEEEEEEEEEEEEEEEEEEEEee
            // Schedule the notification
            val serviceIntent = Intent(contextRef.get(), NotificationService::class.java).apply {
                putExtra(NotificationService.EXTRA_QUESTION_ID, question.question_id)
                putExtra(NotificationService.EXTRA_QUESTION_TITLE, question.question_title)
                putExtra(NotificationService.EXTRA_ANSWERS, question.answers.toTypedArray())
            }
            ContextCompat.startForegroundService(contextRef.get()!!, serviceIntent)
//            return

        } else {
            val serviceIntent = Intent(contextRef.get(), NotificationService::class.java).apply {
                putExtra(NotificationService.EXTRA_QUESTION_ID, question.question_id)
                putExtra(NotificationService.EXTRA_QUESTION_TITLE, question.question_title)
                putExtra(NotificationService.EXTRA_ANSWERS, question.answers.toTypedArray())
            }
            ContextCompat.startForegroundService(contextRef.get()!!, serviceIntent)
//            notificationManager.sendNotification(question)
        }
    }

    private fun isAppInForeground(context: Context): Boolean {
        val app = context.applicationContext
        return if (app is MyApplication) {
            app.isAppInForeground
        } else {
            // Fallback if MyApplication is not set up
            true
        }
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

    fun initializeWorkflow(workflowString: String, selectedWorkflowName: String): Workflow? {
        Log.d(TAG, "Initializing workflow. Selected workflow name: $selectedWorkflowName")
        val gson = Gson()
        val workflowListType = object : TypeToken<List<Workflow>>() {}.type

        return try {
            workflowContent = workflowString
            workflows = gson.fromJson(workflowString, workflowListType)
            // Log.d(TAG, "Parsed workflows: ${workflows.size}")

            selectedWorkflow = workflows.first { it.workflow_name.trim() == selectedWorkflowName.trim() }
            // Log.d(TAG, "Selected Workflow: ${selectedWorkflow.workflow_name}")
            // Log.d(TAG, "Number of questions in selected workflow: ${selectedWorkflow.questions.size}")
            // Log.d(TAG, "Questions: ${selectedWorkflow.questions}")

            // Start the foreground service
            startForegroundService()

            selectedWorkflow
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing workflow JSON: ${e.message}", e)
            null
        }
    }

    private fun startForegroundService() {
        val context = contextRef.get() ?: return
        val serviceIntent = Intent(context, NotificationService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.d(TAG, "Started foreground service")
    }

    fun scheduleCustomDialogs(workflow: Workflow) {
        workflowScheduler.scheduleDialogs(workflow)
    }

    fun cancelScheduledDialogs() {
        workflowScheduler.cancelScheduledDialogs()
        dialogManager.dismissAllDialogs()
    }

    fun rescheduleDialogs() {
        Log.d(TAG, "Rescheduling dialogs")
        if (::selectedWorkflow.isInitialized) {
            workflowScheduler.rescheduleDialogs(selectedWorkflow)
        } else {
            Log.e(TAG, "Cannot reschedule dialogs: selectedWorkflow is not initialized")
        }
    }

    companion object {
        const val PREF_VIBRATION = "checkBoxVibration"
        const val PREF_SOUND = "checkBoxSound"
    }
}
