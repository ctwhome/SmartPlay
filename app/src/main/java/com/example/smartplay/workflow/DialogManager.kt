package com.example.smartplay.workflow

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.smartplay.R
import com.example.smartplay.ui.FlowLayout

class DialogManager(private val context: Context, private val recordAnswer: (Question, String) -> Unit) {
    private val TAG = "DialogManager"
    private val activeDialogs = mutableMapOf<Int, AlertDialog>()

    fun showCustomDialog(question: Question) {
        Log.d(TAG, "Showing custom dialog for question: ${question.question_id}")

        val existingDialog = activeDialogs[question.question_id]
        if (existingDialog != null) {
            Log.d(TAG, "Dismissing existing dialog for question ${question.question_id}")
            existingDialog.setOnDismissListener {
                activeDialogs.remove(question.question_id)
                showNewDialog(question)
            }
            existingDialog.dismiss()
        } else {
            showNewDialog(question)
        }
    }

    private fun showNewDialog(question: Question) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)

        val dialog = builder.create()
        val dialogView = createCustomDialogView(question, dialog)
        dialog.setView(dialogView)

        dialog.setOnDismissListener {
            activeDialogs.remove(question.question_id)
        }

        dialog.show()
        Log.d(TAG, "Custom alert dialog shown for question: ${question.question_id}")

        activeDialogs[question.question_id] = dialog
    }

    private fun createCustomDialogView(question: Question, dialog: AlertDialog): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_custom_answers, null)

        val questionTitle = view.findViewById<TextView>(R.id.questionTitle)
        questionTitle.text = question.question_title

        val answersLayout = view.findViewById<FlowLayout>(R.id.answersLayout)

        question.answers.forEach { answer ->
            val button = Button(context).apply {
                text = answer
                setOnClickListener {
                    recordAnswer(question, answer)
                    dialog.dismiss()
                    activeDialogs.remove(question.question_id)
                }
            }
            answersLayout.addView(button)
        }

        return view
    }

    fun dismissAllDialogs() {
        activeDialogs.values.forEach { it.dismiss() }
        activeDialogs.clear()
    }
}