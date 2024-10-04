package com.example.smartplay.sensors

import android.app.Activity
import com.example.smartplay.recording.AudioRecorder

class AudioRecorderManager(private val activity: Activity) {
    private val audioRecorder = AudioRecorder(activity)

    fun startRecording() {
        audioRecorder.startRecording()
    }

    fun stopRecording() {
        audioRecorder.stopRecording()
    }

    fun isRecording(): Boolean {
        return audioRecorder.isRecording
    }

    fun checkAndRequestAudioPermission(): Boolean {
        return audioRecorder.checkAndRequestAudioPermission()
    }
}