package com.example.smartplay.workflow

import android.content.Context
import com.example.smartplay.recording.DataRecorder

class WorkflowHandler(private val context: Context) {
    private val dataRecorder = DataRecorder(context)
    private val workflowManager = WorkflowManager(context, dataRecorder)

    fun initializeWorkflow(workflowFileContent: String, selectedWorkflowName: String): Workflow? {
        return workflowManager.initializeWorkflow(workflowFileContent, selectedWorkflowName)
    }

    fun startWorkflow() {
        workflowManager.startWorkflow()
    }

    fun stopWorkflow() {
        workflowManager.stopWorkflow()
    }
}