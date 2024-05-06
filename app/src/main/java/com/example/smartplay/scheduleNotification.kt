package com.example.smartplay

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

// Example function to schedule an alarm using AlarmManager
fun scheduleNotification(context: Context, question: Question, workflowName: String) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("question_id", question.question_id)
        putExtra("question_title", question.question_title)
        putExtra("answers", question.answers.toTypedArray())
        putExtra("workflow_name", workflowName)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context, question.question_id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerTimeMillis = System.currentTimeMillis() + question.time_after_start_in_minutes * 60 * 1000
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
}

// Example usage
// for (workflow in workflows) {
//    for (question in workflow.questions) {
//        scheduleNotification(context, question, workflow.workflow_name)
//    }
// }
