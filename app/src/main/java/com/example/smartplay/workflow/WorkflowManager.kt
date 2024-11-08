package com.example.smartplay.workflow

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.smartplay.sensors.SoundAndVibrationManager
import com.example.smartplay.workflow.notifications.NotificationManager
import com.example.smartplay.workflow.notifications.NotificationService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference

class WorkflowManager(
    context: Context,
) {
    private val TAG = "WorkflowManager"
    private lateinit var workflows: List<Workflow>
    private lateinit var selectedWorkflow: Workflow
    private lateinit var workflowContent: String
    private val contextRef: WeakReference<Context> = WeakReference(context)

//    private val dialogManager: DialogManager
    private val notificationManager: NotificationManager
    private val soundAndVibrationManager: SoundAndVibrationManager

    init {
        Log.d(TAG, "Initializing WorkflowManager")
//        dialogManager = DialogManager(context)
        notificationManager = NotificationManager(context)
        soundAndVibrationManager = SoundAndVibrationManager(context)
        // start the foreground service
        ContextCompat.startForegroundService(context, Intent(context, NotificationService::class.java))
        Log.d(TAG, "NotificationService started")
    }

    private fun startWorkflowService() {
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

    fun initializeWorkflow(workflowString: String, selectedWorkflowName: String): Workflow? {
        Log.d(TAG, "Initializing workflow. Selected workflow name: $selectedWorkflowName")
        val gson = Gson()
        val workflowListType = object : TypeToken<List<Workflow>>() {}.type

        return try {
            workflowContent = workflowString
            workflows = gson.fromJson(workflowString, workflowListType)
            Log.d(TAG, "Parsed workflows: ${workflows.size}")

            selectedWorkflow = workflows.first { it.workflow_name.trim() == selectedWorkflowName.trim() }

            // We'll let the caller decide when to start the workflow
            // startWorkflowService()

            selectedWorkflow
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing workflow JSON: ${e.message}", e)
            null
        }
    }

    fun startWorkflow() {
        Log.d(TAG, "Starting workflow")
        if (::selectedWorkflow.isInitialized) {
            startWorkflowService()
        } else {
            Log.e(TAG, "Cannot start workflow: No workflow has been initialized")
        }
    }

    fun stopWorkflow() {
        Log.d(TAG, "Stopping workflow")
        val context = contextRef.get()
        if (context != null) {
            val serviceIntent = Intent(context, WorkflowService::class.java)
            context.stopService(serviceIntent)
            Log.d(TAG, "WorkflowService stopped")
        } else {
            Log.e(TAG, "Context is null, cannot stop WorkflowService")
        }
    }
}
