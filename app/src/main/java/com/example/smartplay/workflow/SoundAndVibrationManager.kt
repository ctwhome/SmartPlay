package com.example.smartplay.workflow

import android.content.Context
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.smartplay.R

class SoundAndVibrationManager(private val context: Context) {
    private val TAG = "SoundAndVibrationManager"

    fun playSound() {
        try {
            Log.d(TAG, "Attempting to play fallback sound")
            val mediaPlayer = MediaPlayer.create(context, R.raw.fallback_sound)
            if (mediaPlayer == null) {
                Log.e(TAG, "Fallback MediaPlayer creation failed")
                return
            }
            mediaPlayer.setOnCompletionListener { mp ->
                Log.d(TAG, "Fallback sound playback completed")
                mp.release()
            }
            mediaPlayer.start()
            Log.d(TAG, "Fallback sound playback started")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing fallback sound: ${e.message}")
        }
    }

    fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
        Log.d(TAG, "Vibration executed")
    }
}