package com.example.smartplay.workflow

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.smartplay.data.DataRecorder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkflowManager(private val context: Context, private val dataRecorder: DataRecorder) {
    private val TAG = "WorkflowManager"
    private lateinit var workflows: List<Workflow>
    private lateinit var selectedWorkflow: Workflow
    private lateinit var workflowContent: String // New property to store the raw JSON string

    fun initializeWorkflow(workflowString: String, selectedWorkflowName: String): Workflow? {
        Log.d(TAG, "Initializing workflow. Selected workflow name: $selectedWorkflowName")
        val gson = Gson()
        val workflowListType = object : TypeToken<List<Workflow>>() {}.type

        return try {
            workflowContent = workflowString // Store the raw JSON string
            workflows = gson.fromJson(workflowString, workflowListType)
            Log.d(TAG, "Parsed workflows: ${workflows.size}")

            selectedWorkflow =
                    workflows.first { it.workflow_name.trim() == selectedWorkflowName.trim() }
            Log.d(TAG, "Selected Workflow: ${selectedWorkflow.workflow_name}")
            Log.d(
                    TAG,
                    "Number of questions in selected workflow: ${selectedWorkflow.questions.size}"
            )
            selectedWorkflow
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing workflow JSON: ${e.message}", e)
            null
        }
    }

    fun getWorkflowContent(): String {
        return workflowContent
    }

    fun scheduleCustomDialogs(workflow: Workflow) {
        Log.d(TAG, "Scheduling custom dialogs for workflow: ${workflow.workflow_name}")
        workflow.questions.forEachIndexed { index, question ->
            Log.d(TAG, "Scheduling dialog for question ${index + 1}: ${question.question_id}")
            scheduleDialog(question)
        }
    }

    private fun scheduleDialog(question: Question) {
        val handler = Handler(Looper.getMainLooper())
        val delayMillis = question.time_after_start.toLong() * 1000
        Log.d(
                TAG,
                "Scheduling dialog for question ${question.question_id} with delay: $delayMillis ms"
        )
        handler.postDelayed(
                {
                    Log.d(TAG, "Showing dialog for question ${question.question_id}")
                    showCustomDialog(question)
                },
                delayMillis
        )
    }

    private fun showCustomDialog(question: Question) {
        Log.d(TAG, "Showing custom dialog for question: ${question.question_id}")
        val builder = AlertDialog.Builder(context)
        builder.setTitle(question.question_text)

        when (question.question_type) {
            "open" -> showOpenDialog(builder, question)
            "multiple_choice" -> showMultipleChoiceDialog(builder, question)
            else -> Log.e(TAG, "Unknown question type: ${question.question_type}")
        }
    }

    private fun showOpenDialog(builder: AlertDialog.Builder, question: Question) {
        Log.d(TAG, "Showing open dialog for question: ${question.question_id}")
        val input = android.widget.EditText(context)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val answer = input.text.toString()
            recordAnswer(question, answer)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showMultipleChoiceDialog(builder: AlertDialog.Builder, question: Question) {
        Log.d(TAG, "Showing multiple choice dialog for question: ${question.question_id}")
        val options = question.options.toTypedArray()
        builder.setItems(options) { dialog, which ->
            val selectedOption = options[which]
            recordAnswer(question, selectedOption)
            dialog.dismiss()
        }
        builder.show()
    }

    private fun recordAnswer(question: Question, answer: String) {
        val timestamp = System.currentTimeMillis()
        dataRecorder.writeQuestionData(
                timestamp,
                question.question_id,
                question.question_text,
                answer
        )
        Log.d(TAG, "Answer recorded: ${question.question_id}, $answer")
    }
}

data class Workflow(val workflow_name: String, val questions: List<Question>)

data class Question(
        val question_id: String,
        val question_text: String,
        val question_type: String,
        val time_after_start: Int,
        val options: List<String> = emptyList()
)
