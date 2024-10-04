package com.example.smartplay.recording

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException

class AudioRecorder(private val activity: Activity) {

    companion object {
        private const val TAG = "AudioRecorderDebug"
    }

    private var mediaRecorder: MediaRecorder? = null
    var isRecording = false
        private set

    private lateinit var audioFile: File

    private fun getWatchId(context: Context): String {
        return android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
    }

    fun startRecording(): Boolean {
        Log.d(TAG, "startRecording() called")

        if (isRecording) {
            Log.d(TAG, "Already recording, returning")
            return true
        }

        if (!checkPermissions()) {
            Log.d(TAG, "Permissions not granted, returning")
            return false
        }

        val sharedPref = activity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val childId = sharedPref.getString("idChild", "000") ?: "000"
        val timestamp = System.currentTimeMillis()
        val watchId = getWatchId(activity)
        val dir = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        if (dir == null) {
            Log.e(TAG, "Failed to get external files directory")
            Toast.makeText(activity, "Failed to get storage directory", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "Failed to create directory: ${dir.absolutePath}")
            Toast.makeText(activity, "Failed to create storage directory", Toast.LENGTH_SHORT).show()
            return false
        }

        audioFile = File(dir, "${childId}_AUDIO_${watchId}_${timestamp}.3gp")

        Log.d(TAG, "Audio file path: ${audioFile.absolutePath}")
        Log.d(TAG, "Directory exists: ${dir.exists()}")
        Log.d(TAG, "Directory can write: ${dir.canWrite()}")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                Log.d(TAG, "MediaRecorder prepared successfully, file: ${audioFile.absolutePath}")
            } catch (e: IOException) {
                Log.e(TAG, "Prepare failed: ${e.message}")
                e.printStackTrace()
                return false
            }

            try {
                start()
                Log.d(TAG, "Recording started")
                isRecording = true
//                Toast.makeText(activity, "Recording started", Toast.LENGTH_SHORT).show()
                return true
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Start failed: ${e.message}")
                e.printStackTrace()
                return false
            }
        }
    }

    fun stopRecording(): String? {
        Log.d(TAG, "stopRecording() called")

        if (!isRecording) {
            Log.d(TAG, "Attempted to stop recording, but not currently recording")
            return null
        }

        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            Log.d(TAG, "Recording stopped")
//            Toast.makeText(activity, "Recording stopped", Toast.LENGTH_SHORT).show()

            if (audioFile.exists() && audioFile.length() > 0) {
                Log.d(TAG, "Audio file successfully saved: ${audioFile.absolutePath}")
                audioFile.absolutePath
            } else {
                Log.e(TAG, "Audio file does not exist or is empty")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), 200)
            return false
        }

        return true
    }

    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == 200) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d(TAG, "All required permissions granted")
                startRecording()
            } else {
                Log.e(TAG, "Not all permissions were granted")
                Toast.makeText(activity, "Permissions required for audio recording", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkAndRequestAudioPermission(): Boolean {
        return checkPermissions()
    }
}
