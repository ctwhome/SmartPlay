package com.example.smartplay.workflow

import android.os.Handler
import android.os.Looper
import android.util.Log

class WorkflowScheduler(private val showQuestion: (Question) -> Unit) {
    private val TAG = "WorkflowScheduler"
    private val handler = Handler(Looper.getMainLooper())
    private val scheduledRunnables = mutableListOf<Runnable>()

    fun scheduleDialogs(workflow: Workflow) {
        Log.d(TAG, "Scheduling dialogs for workflow: ${workflow.workflow_name}")
        workflow.questions.forEachIndexed { index, question ->
            Log.d(TAG, "Scheduling dialog for question ${index + 1}: ${question.question_id}")
            scheduleDialog(question)
        }
    }

    private fun scheduleDialog(question: Question) {
        val delayMillis = question.time_after_start * 1000L
        Log.d(TAG, "Scheduling dialog for question ${question.question_id} with delay: $delayMillis ms")
        val runnable = Runnable {
            Log.d(TAG, "Executing runnable for question ${question.question_id}")
            showQuestion(question)
        }
        scheduledRunnables.add(runnable)
        handler.postDelayed(runnable, delayMillis)
        Log.d(TAG, "Dialog scheduled for question ${question.question_id}")

        // Schedule repeated questions if frequency > 1
        if (question.frequency > 1) {
            for (i in 1 until question.frequency) {
                val repeatedDelayMillis = delayMillis + (question.time_between_repetitions * 1000L * i)
                val repeatedRunnable = Runnable {
                    Log.d(TAG, "Executing repeated runnable for question ${question.question_id}, repetition ${i + 1}")
                    showQuestion(question)
                }
                scheduledRunnables.add(repeatedRunnable)
                handler.postDelayed(repeatedRunnable, repeatedDelayMillis)
                Log.d(TAG, "Repeated dialog scheduled for question ${question.question_id}, repetition ${i + 1} with delay: $repeatedDelayMillis ms")
            }
        }
    }

    fun cancelScheduledDialogs() {
        Log.d(TAG, "Cancelling all scheduled dialogs")
        scheduledRunnables.forEach { handler.removeCallbacks(it) }
        scheduledRunnables.clear()
        Log.d(TAG, "All scheduled dialogs cancelled")
    }

    fun rescheduleDialogs(workflow: Workflow) {
        Log.d(TAG, "Rescheduling dialogs")
        cancelScheduledDialogs()
        scheduleDialogs(workflow)
        Log.d(TAG, "Dialogs rescheduled for workflow: ${workflow.workflow_name}")
    }
}