package com.example.smartplay

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.smartplay.workflow.QuestionRecorder
import com.example.smartplay.data.DataRecorder
import com.example.smartplay.workflow.dialogs.DialogBroadcastReceiver
import java.text.SimpleDateFormat
import java.util.*

class MyApplication : Application(), LifecycleObserver, QuestionRecorder {
    var isAppInForeground = false
    private lateinit var dataRecorder: DataRecorder
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dialogReceiver: DialogBroadcastReceiver

    var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        dataRecorder = DataRecorder(this)
        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        // Initialize DataRecorder
        initializeDataRecorder()

        // Register DialogBroadcastReceiver
        dialogReceiver = DialogBroadcastReceiver()
        registerReceiver(dialogReceiver, IntentFilter("com.example.smartplay.SHOW_DIALOG"))

        // Register activity lifecycle callbacks
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }
            override fun onActivityPaused(activity: Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun initializeDataRecorder() {
        val childId = sharedPreferences.getString("idChild", "001") ?: "001"
        val watchId = getDeviceId()
        val timestamp = System.currentTimeMillis()
        dataRecorder.initializeFiles(childId, watchId, timestamp)
    }

    private fun getDeviceId(): String {
        // Implement a method to get a unique device ID or use a default value
        return "watch001"
    }

    override fun onTerminate() {
        super.onTerminate()
        // Unregister DialogBroadcastReceiver
        unregisterReceiver(dialogReceiver)
        // Close DataRecorder files
        dataRecorder.closeFiles()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        isAppInForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        isAppInForeground = false
    }

    override fun writeQuestionsToCSV(
        timestamp: Long,
        questionId: String,
        questionTitle: String,
        answer: String,
        state: String
    ) {
        dataRecorder.writeQuestionData(timestamp, questionId, questionTitle, answer, state)
    }
}