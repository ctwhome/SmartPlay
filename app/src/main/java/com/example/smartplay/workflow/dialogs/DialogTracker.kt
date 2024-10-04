package com.example.smartplay.workflow.dialogs

import android.app.Dialog
import android.util.Log

object DialogTracker {
    private val TAG = "DialogTracker"
    private val openDialogs = mutableMapOf<Int, MutableList<Dialog>>()

    fun addDialog(questionId: Int, dialog: Dialog) {
        openDialogs.getOrPut(questionId) { mutableListOf() }.add(dialog)
        Log.d(TAG, "Added dialog for question ID: $questionId. Total open dialogs for this question: ${openDialogs[questionId]?.size}")
    }

    fun removeDialog(questionId: Int, dialog: Dialog) {
        openDialogs[questionId]?.remove(dialog)
        if (openDialogs[questionId]?.isEmpty() == true) {
            openDialogs.remove(questionId)
        }
        Log.d(TAG, "Removed dialog for question ID: $questionId. Total open dialogs for this question: ${openDialogs[questionId]?.size ?: 0}")
    }

    fun closeDialogsForQuestion(questionId: Int) {
        openDialogs[questionId]?.forEach { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
                Log.d(TAG, "Closed dialog for question ID: $questionId")
            }
        }
        openDialogs.remove(questionId)
        Log.d(TAG, "Closed all dialogs for question ID: $questionId")
    }

    fun closeAllDialogs() {
        openDialogs.forEach { (questionId, dialogs) ->
            dialogs.forEach { dialog ->
                if (dialog.isShowing) {
                    dialog.dismiss()
                    Log.d(TAG, "Closed dialog for question ID: $questionId")
                }
            }
        }
        openDialogs.clear()
        Log.d(TAG, "Closed all dialogs. Total open dialogs: ${openDialogs.size}")
    }

    fun hasDialogsForQuestion(questionId: Int): Boolean {
        return openDialogs.containsKey(questionId) && openDialogs[questionId]?.isNotEmpty() == true
    }
}