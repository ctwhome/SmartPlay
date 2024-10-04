package com.example.smartplay.workflow.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.smartplay.R
import com.example.smartplay.recording.FlowLayout
import com.example.smartplay.workflow.Question
import com.example.smartplay.RecordingActivity

class DialogManager private constructor() {
    private val TAG = "DialogManager"
    private lateinit var applicationContext: Context

    companion object {
        @Volatile
        private var instance: DialogManager? = null

        fun getInstance(): DialogManager {
            return instance ?: synchronized(this) {
                instance ?: DialogManager().also { instance = it }
            }
        }
    }

    fun init(context: Context) {
        if (!::applicationContext.isInitialized) {
            applicationContext = context.applicationContext
        }
    }

    fun showCustomDialog(question: Question, activity: Activity) {
        Log.d(TAG, "Attempting to show custom dialog for question: ${question.question_id}")
        activity.runOnUiThread {
            if (DialogTracker.hasDialogsForQuestion(question.question_id)) {
                Log.d(TAG, "Closing existing dialogs for question: ${question.question_id}")
                DialogTracker.closeDialogsForQuestion(question.question_id)
            }
            createAndShowDialog(question, activity)
        }
    }

    private fun createAndShowDialog(question: Question, activity: Activity) {
        Log.d(TAG, "Creating new dialog for question: ${question.question_id}")
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)

        val dialog = builder.create()
        val dialogView = createCustomDialogView(question, dialog, activity)
        dialog.setView(dialogView)

        dialog.setOnDismissListener {
            Log.d(TAG, "Dialog dismissed for question: ${question.question_id}")
            DialogTracker.removeDialog(question.question_id, dialog)
        }

        dialog.show()
        Log.d(TAG, "Custom alert dialog shown for question: ${question.question_id}")

        DialogTracker.addDialog(question.question_id, dialog)
    }

    private fun createCustomDialogView(question: Question, dialog: AlertDialog, activity: Activity): View {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.dialog_custom_answers, null)

        val questionTitle = view.findViewById<TextView>(R.id.questionTitle)
        questionTitle.text = question.question_title

        val answersLayout = view.findViewById<FlowLayout>(R.id.answersLayout)

        question.answers.forEach { answer ->
            val button = Button(activity).apply {
                text = answer
                setOnClickListener {
                    Log.d(TAG, "Answer selected for question: ${question.question_id}")
                    val timestamp = System.currentTimeMillis()
                    (activity as? RecordingActivity)?.recordQuestionAnswered(
                        timestamp,
                        question.question_id.toString(),
                        question.question_title,
                        answer
                    )
                    dialog.dismiss()
                    DialogTracker.removeDialog(question.question_id, dialog)
                }
            }
            answersLayout.addView(button)
        }

        return view
    }

    fun dismissAllDialogs() {
        Log.d(TAG, "Dismissing all dialogs")
        DialogTracker.closeAllDialogs()
    }
}
