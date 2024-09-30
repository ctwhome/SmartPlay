package com.example.smartplay

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.smartplay.workflow.QuestionRecorder
import com.example.smartplay.data.DataRecorder

class MyApplication : Application(), LifecycleObserver, QuestionRecorder {
    var isAppInForeground = false
    private lateinit var dataRecorder: DataRecorder
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        dataRecorder = DataRecorder(this)
        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        initializeDataRecorder()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        isAppInForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        isAppInForeground = false
    }

    private fun initializeDataRecorder() {
        val childId = sharedPreferences.getString("idChild", "000")
        val watchId = getWatchId()
        val timestamp = System.currentTimeMillis()
        dataRecorder.initializeFiles(childId ?: "000", watchId, timestamp)
    }

    private fun getWatchId(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
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