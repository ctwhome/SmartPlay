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
import com.example.smartplay.recording.FlowLayout // Reverted back to the recording package
import com.example.smartplay.workflow.Question
import com.example.smartplay.RecordingActivity

class DialogManager private constructor() {
    private val TAG = "DialogManager"
    private lateinit var applicationContext: Context

    companion object {
        @Volatile
        private var instance: DialogManager? = null

        private val activeDialogs = mutableMapOf<Int, AlertDialog>()

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
            activeDialogs[question.question_id]?.let { existingDialog ->
                if (existingDialog.isShowing) {
                    existingDialog.dismiss()
                    Log.d(TAG, "Dismissed existing dialog for question: ${question.question_id}")
                }
                activeDialogs.remove(question.question_id)
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
            activeDialogs.remove(question.question_id)
            Log.d(TAG, "Active dialogs after dismissal: ${activeDialogs.size}")
        }

        dialog.show()
        Log.d(TAG, "Custom alert dialog shown for question: ${question.question_id}")

        activeDialogs[question.question_id] = dialog
        Log.d(TAG, "Total active dialogs: ${activeDialogs.size}")
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
                    activeDialogs.remove(question.question_id)
                }
            }
            answersLayout.addView(button)
        }

        return view
    }

    private fun closeAllDialogs(activity: Activity) {
        Log.d(TAG, "Closing all dialogs")
        activity.runOnUiThread {
            val dialogsToClose = activeDialogs.values.toList()
            dialogsToClose.forEach { dialog ->
                if (dialog.isShowing) {
                    dialog.dismiss()
                    Log.d(TAG, "Dismissed a dialog")
                }
            }
            activeDialogs.clear()
            Log.d(TAG, "All dialogs closed and cleared. Active dialogs: ${activeDialogs.size}")
        }
    }

    fun dismissAllDialogs(activity: Activity) {
        Log.d(TAG, "Dismissing all dialogs")
        closeAllDialogs(activity)
    }
}
