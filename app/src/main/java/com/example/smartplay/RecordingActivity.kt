package com.example.smartplay

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.app.AlertDialog
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Handler
import android.os.Looper
import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartplay.sensors.CustomSensorManager
import com.example.smartplay.location.CustomLocationManager
import com.example.smartplay.bluetooth.CustomBluetoothManager
import com.example.smartplay.data.DataRecorder
import com.example.smartplay.workflow.WorkflowManager
import com.example.smartplay.utils.QuestionRecorder
import com.example.smartplay.utils.AudioRecorder

class RecordingActivity : AppCompatActivity(), QuestionRecorder {

    private lateinit var sensorManager: CustomSensorManager
    private lateinit var locationManager: CustomLocationManager
    private lateinit var bluetoothManager: CustomBluetoothManager
    private lateinit var dataRecorder: DataRecorder
    private lateinit var workflowManager: WorkflowManager
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var sensorDataTextView: TextView

    private var isRecording = false
    private var lastUpdateTime: Long = 0
    private var scannedDevices: Map<String, Int> = emptyMap()

    private val passwordActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                stopRecording()
                finish() // Navigate back to the previous activity (settings)
            }
            RESULT_CANCELED -> {
                // User canceled, continue recording
                Log.d(TAG, "Password entry canceled, continuing recording")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recording_activity)

        supportActionBar?.hide() // Hide the action bar

        // Keep this activity in focus
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)
        sensorDataTextView = findViewById(R.id.sensorData)

        initializeManagers()

        startButton.setOnClickListener {
            if (!isRecording) {
                startButton.visibility = Button.GONE
                stopButton.visibility = Button.VISIBLE
                startRecording()
            }
        }

        stopButton.setOnClickListener {
            if (isRecording) {
                startButton.visibility = Button.VISIBLE
                stopButton.visibility = Button.GONE
                showPasswordActivity()
            }
        }

        // Start recording immediately when the activity is created
        Log.d(TAG, "Calling startRecording from onCreate")
        startRecording()

        // Set initial visibility of buttons based on recording state
        startButton.visibility = if (isRecording) Button.GONE else Button.VISIBLE
        stopButton.visibility = if (isRecording) Button.VISIBLE else Button.GONE

        // Set initial visibility of sensor data
        updateSensorDataVisibility()
    }

    private fun initializeManagers() {
        sensorManager = CustomSensorManager(this)
        locationManager = CustomLocationManager(this)
        bluetoothManager = CustomBluetoothManager(this)
        dataRecorder = DataRecorder(this)
        workflowManager = WorkflowManager(this, dataRecorder)
        audioRecorder = AudioRecorder(this)

        // Set up Bluetooth scan result listener
        bluetoothManager.setOnScanResultListener { devices ->
            scannedDevices = devices
        }
    }

    private fun showPasswordActivity() {
        val intent = Intent(this, PasswordActivity::class.java)
        passwordActivityLauncher.launch(intent)
    }

    private fun startRecording() {
        Log.d(TAG, "startRecording() called")
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val childId = sharedPref.getString("idChild", "000")
        val checkBoxAudioRecording = sharedPref.getString("checkBoxAudioRecording", "true")
        val timestamp = System.currentTimeMillis()
        val watchId = getWatchId(this)

        Log.d(TAG, "childId: $childId, checkBoxAudioRecording: $checkBoxAudioRecording")

        dataRecorder.initializeFiles(childId ?: "000", watchId, timestamp)

        // Start managers
        sensorManager.startListening()
        locationManager.startListening()
        bluetoothManager.startScanning(sharedPref.getString("frequencyRate", "1000")?.toLong() ?: 1000)

        Log.d(TAG, "checkAudioPermission() result: ${checkAudioPermission()}")
        // Record audio
        if (checkBoxAudioRecording?.toBoolean() == true) {
            Log.d(TAG, "Audio recording is enabled in preferences")
            if (checkAudioPermission()) {
                Log.d(TAG, "Starting audio recording")
                if (audioRecorder.startRecording()) {
                    Log.d(TAG, "Audio recording started successfully")
                } else {
                    Log.e(TAG, "Failed to start audio recording")
                    // Attempt to start recording again
                    Handler(Looper.getMainLooper()).postDelayed({
                        Log.d(TAG, "Attempting to start audio recording again")
                        audioRecorder.startRecording()
                    }, 1000)
                }
            } else {
                Log.e(TAG, "Audio recording permission not granted")
            }
        } else {
            Log.d(TAG, "Audio recording is disabled in preferences")
        }

        isRecording = true

        // Initialize the workflow questions
        initWorkflowQuestions()

        // Start updating UI
        startUpdatingUI()

        // Update sensor data visibility
        updateSensorDataVisibility()
    }

    private fun stopRecording() {
        Log.d(TAG, "stopRecording() called")
        isRecording = false
        sensorManager.stopListening()
        locationManager.stopListening()
        bluetoothManager.stopScanning()
        dataRecorder.closeFiles()

        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val checkBoxAudioRecording = sharedPref.getString("checkBoxAudioRecording", "true")
        // Stop audio recording
        if (checkBoxAudioRecording?.toBoolean() == true) {
            Log.d(TAG, "Stopping audio recording")
            val audioFilePath = audioRecorder.stopRecording()
            if (audioFilePath != null) {
                Log.d(TAG, "Audio recording stopped successfully. File saved at: $audioFilePath")
            } else {
                Log.e(TAG, "Failed to stop audio recording or save audio file")
            }
        } else {
            Log.d(TAG, "Audio recording was not active")
        }
    }

    private fun startUpdatingUI() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (isRecording && SystemClock.elapsedRealtime() - lastUpdateTime > 1000) {
                    updateUI()
                    lastUpdateTime = SystemClock.elapsedRealtime()
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun updateUI() {
        val timestamp = System.currentTimeMillis()
        sensorDataTextView.text = """
        ⏱️ $timestamp
        ❤️ ${sensorManager.heartRate}
        🌍 ${locationManager.latitude} ${locationManager.longitude}
        🧭 ${sensorManager.magnetoX} ${sensorManager.magnetoY} ${sensorManager.magnetoZ}
        🔀 ${sensorManager.gyroX} ${sensorManager.gyroY} ${sensorManager.gyroZ}
        🏎️ ${sensorManager.accelX} ${sensorManager.accelY} ${sensorManager.accelZ}
        👣 ${sensorManager.sessionSteps}
        📡 ${scannedDevices}
        🎙️ ${if (audioRecorder.isRecording) "Recording" else "Not Recording"}
        """.trimIndent()

        val sensorDataMap = mapOf(
            "timestamp" to timestamp,
            "latitude" to locationManager.latitude,
            "longitude" to locationManager.longitude,
            "heartRate" to sensorManager.heartRate,
            "accelX" to sensorManager.accelX,
            "accelY" to sensorManager.accelY,
            "accelZ" to sensorManager.accelZ,
            "gyroX" to sensorManager.gyroX,
            "gyroY" to sensorManager.gyroY,
            "gyroZ" to sensorManager.gyroZ,
            "magnetoX" to sensorManager.magnetoX,
            "magnetoY" to sensorManager.magnetoY,
            "magnetoZ" to sensorManager.magnetoZ,
            "steps" to sensorManager.sessionSteps
        )

        dataRecorder.writeSensorData(sensorDataMap)
        dataRecorder.writeBluetoothData(timestamp, scannedDevices)
    }

    private fun updateSensorDataVisibility() {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val displaySensorValues = sharedPref.getString("checkBoxDisplaySensorValues", "false")?.toBoolean() ?: true
        sensorDataTextView.visibility = if (displaySensorValues) View.VISIBLE else View.GONE
    }

    private fun initWorkflowQuestions() {
        val sharedData = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val workflowString = sharedData.getString("workflowFile", "")
        val selectedWorkflowName = sharedData.getString("selectedWorkflow", "NOT_FOUND") ?: "NOT_FOUND"

        if (!workflowString.isNullOrEmpty() && selectedWorkflowName != "NOT_FOUND") {
            try {
                val workflow = workflowManager.initializeWorkflow(workflowString, selectedWorkflowName)
                if (workflow != null) {
                    workflowManager.scheduleCustomDialogs(workflow)
                } else {
                    Log.e(TAG, "Workflow initialization failed: Null workflow returned")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Workflow initialization failed: ${e.message}", e)
            }
        } else {
            Log.e(TAG, "Workflow initialization failed: Missing data")
        }
    }

    private fun getWatchId(context: Context): String {
        return android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
    }

    private fun checkAudioPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting audio permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_REQUEST_CODE)
            return false
        }
        Log.d(TAG, "Audio permission already granted")
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Audio recording permission granted")
                    audioRecorder.startRecording()
                } else {
                    Log.e(TAG, "Audio recording permission denied")
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isRecording) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Recording in progress. Do you really want to leave?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ -> super.onBackPressed() }
                .setNegativeButton("No", null)
            val alert = builder.create()
            alert.show()
        } else {
            super.onBackPressed()
        }
    }

    override fun writeQuestionsToCSV(timestamp: Long, questionId: String, questionTitle: String, answer: String) {
        dataRecorder.writeQuestionData(timestamp, questionId, questionTitle, answer)
    }

    companion object {
        private const val TAG = "RecordingActivity"
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1001
    }
}
