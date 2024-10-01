package com.example.smartplay.workflow

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.smartplay.data.DataRecorder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference

class WorkflowManager(
    context: Context,
    private val dataRecorder: DataRecorder,
) {
    private val TAG = "WorkflowManager"
    private lateinit var workflows: List<Workflow>
    private lateinit var selectedWorkflow: Workflow
    private lateinit var workflowContent: String
    private val contextRef: WeakReference<Context> = WeakReference(context)

    private val dialogManager: DialogManager
    private val notificationManager: NotificationManager
    private val soundAndVibrationManager: SoundAndVibrationManager

    init {
        Log.d(TAG, "Initializing WorkflowManager")
        dialogManager = DialogManager(context) { question, answer -> recordAnswer(question, answer) }
        notificationManager = NotificationManager(context)
        soundAndVibrationManager = SoundAndVibrationManager(context)
        // start the foreground service
        ContextCompat.startForegroundService(context, Intent(context, NotificationService::class.java))
        Log.d(TAG, "NotificationService started")
    }

    private fun startWorkflowService() {
        Log.d(TAG, "Attempting to start WorkflowService")
        val context = contextRef.get()
        if (context == null) {
            Log.e(TAG, "Context is null, cannot start WorkflowService")
            return
        }
        if (!::selectedWorkflow.isInitialized) {
            Log.e(TAG, "selectedWorkflow is not initialized, cannot start WorkflowService")
            return
        }
        val serviceIntent = Intent(context, WorkflowService::class.java).apply {
            putExtra("workflow", selectedWorkflow)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.d(TAG, "Started WorkflowService")
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
            Log.d(TAG, "Parsed workflows: ${workflows.size}")

            selectedWorkflow = workflows.first { it.workflow_name.trim() == selectedWorkflowName.trim() }
            Log.d(TAG, "Selected Workflow: ${selectedWorkflow.workflow_name}")
            Log.d(TAG, "Number of questions in selected workflow: ${selectedWorkflow.questions.size}")
            Log.d(TAG, "Questions: ${selectedWorkflow.questions}")

            // Start the workflow service
            startWorkflowService()

            selectedWorkflow
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing workflow JSON: ${e.message}", e)
            null
        }
    }
}
