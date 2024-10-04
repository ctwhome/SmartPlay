package com.example.smartplay.workflow.notifications

import android.util.Log

object NotificationTracker {
    private val TAG = "NotificationTracker"
    private val activeNotifications = mutableSetOf<Int>()

    fun addNotification(questionId: Int) {
        activeNotifications.add(questionId)
        Log.d(TAG, "Added notification for question ID: $questionId. Total active notifications: ${activeNotifications.size}")
    }

    fun removeNotification(questionId: Int) {
        activeNotifications.remove(questionId)
        Log.d(TAG, "Removed notification for question ID: $questionId. Total active notifications: ${activeNotifications.size}")
    }

    fun hasNotification(questionId: Int): Boolean {
        return activeNotifications.contains(questionId)
    }

    fun getAllActiveNotifications(): Set<Int> {
        return activeNotifications.toSet()
    }
}