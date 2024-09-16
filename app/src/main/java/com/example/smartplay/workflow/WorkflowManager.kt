package com.example.smartplay.workflow

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.smartplay.data.DataRecorder

class WorkflowManager(private val context: Context, private val dataRecorder: DataRecorder) {
    private val TAG = "WorkflowManager"
    private lateinit var workflows: List<Workflow>
    private lateinit var selectedWorkflow: Workflow

    fun initializeWorkflow(workflowString: String, selectedWorkflowName: String): Workflow? {
        val gson = Gson()
        val workflowListType = object : TypeToken<List<Workflow>>() {}.type

        return try {
            workflows = gson.fromJson(workflowString, workflowListType)
            Log.d(TAG, "Parsed workflows: ${workflows.size}")

            selectedWorkflow = workflows.first { it.workflow_name.trim() == selectedWorkflowName.trim() }
            Log.d(TAG, "Selected Workflow: ${selectedWorkflow.workflow_name}")
            selectedWorkflow
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing workflow JSON: ${e.message}")
            null
        }
    }

    fun scheduleCustomDialogs(workflow: Workflow) {
        workflow.questions.forEach { question ->
            scheduleDialog(question)
        }
    }

    private fun scheduleDialog(question: Question) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            showCustomDialog(question)
        }, question.time_after_start.toLong() * 1000)
    }

    private fun showCustomDialog(question: Question) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(question.question_text)

        when (question.question_type) {
            "open" -> showOpenDialog(builder, question)
            "multiple_choice" -> showMultipleChoiceDialog(builder, question)
            else -> Log.e(TAG, "Unknown question type: ${question.question_type}")
        }
    }

    private fun showOpenDialog(builder: AlertDialog.Builder, question: Question) {
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
        dataRecorder.writeQuestionData(timestamp, question.question_id, question.question_text, answer)
        Log.d(TAG, "Answer recorded: ${question.question_id}, $answer")
    }
}

data class Workflow(
    val workflow_name: String,
    val questions: List<Question>
)

data class Question(
    val question_id: String,
    val question_text: String,
    val question_type: String,
    val time_after_start: Int,
    val options: List<String> = emptyList()
)