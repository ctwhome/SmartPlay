// AudioRecorder.kt
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

    fun startRecording() {
        Log.d(TAG, "startRecording() called")

        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(
                activity, arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 200
            )
            return
        }

        val sharedPref = activity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val childId = sharedPref.getString("idChild", "000") ?: "000"
        val timestamp = System.currentTimeMillis()
        val watchId = getWatchId(activity)
        val dir = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        audioFile = File(dir, "${childId}_AUDIO_${watchId}_${timestamp}.3gp")

        Log.d(TAG, "Audio file path: ${audioFile.absolutePath}")
        Log.d(TAG, "Directory exists: ${dir?.exists()}")
        Log.d(TAG, "Directory can write: ${dir?.canWrite()}")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                Log.d(TAG, "MediaRecorder prepared successfully, file: ${audioFile.absolutePath}")
            } catch (e: IOException) {
                Toast.makeText(activity, "Prepare failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Prepare failed: ${e.message}")
                e.printStackTrace()
                return
            }

            try {
                start()
                Log.d(TAG, "Recording started")
            } catch (e: IllegalStateException) {
                Toast.makeText(activity, "Start failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Start failed: ${e.message}")
                e.printStackTrace()
                return
            }
        }
        isRecording = true
        Toast.makeText(activity, "Recording started", Toast.LENGTH_SHORT).show()
    }

    fun stopRecording(): String? {
        Log.d(TAG, "stopRecording() called")

        if (!isRecording) {
            Log.d(TAG, "Attempted to stop recording, but not currently recording")
            return null
        }

        mediaRecorder?.apply {
            try {
                stop()
                Log.d(TAG, "Recording stopped")
            } catch (e: RuntimeException) {
                Log.e(TAG, "Stop failed: ${e.message}")
                Toast.makeText(activity, "Stop failed: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                return null
            }
            release()
            Log.d(TAG, "MediaRecorder released")
        }
        mediaRecorder = null
        isRecording = false
        Toast.makeText(activity, "Recording stopped", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "Audio file saved: ${audioFile.absolutePath}")
        Log.d(TAG, "Audio file exists: ${audioFile.exists()}")
        Log.d(TAG, "Audio file size: ${audioFile.length()} bytes")

        return audioFile.absolutePath
    }

    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == 200) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, you can start recording
                Log.d(TAG, "Audio recording permission granted")
                startRecording()
            } else {
                Log.e(TAG, "Permission denied to record audio")
                Toast.makeText(activity, "Permission denied to record audio", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
