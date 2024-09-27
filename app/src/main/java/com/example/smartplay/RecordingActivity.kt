package com.example.smartplay

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartplay.sensors.CustomBluetoothManager
import com.example.smartplay.data.DataRecorder
import com.example.smartplay.sensors.CustomLocationManager
import com.example.smartplay.sensors.CustomSensorManager
import com.example.smartplay.data.AudioRecorder
import com.example.smartplay.workflow.QuestionRecorder
import com.example.smartplay.workflow.WorkflowManager

class RecordingActivity : AppCompatActivity(), QuestionRecorder {

    private lateinit var sensorManager: CustomSensorManager
    private lateinit var locationManager: CustomLocationManager
    private lateinit var bluetoothManager: CustomBluetoothManager
    private lateinit var dataRecorder: DataRecorder
    private lateinit var workflowManager: WorkflowManager
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var sensorDataTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    private var isRecording = false
    private var lastUpdateTime: Long = 0
    private var scannedDevices: Map<String, Int> = emptyMap()

    private val passwordActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    RESULT_OK -> {
                        stopRecording()
                        finish() // Navigate back to the previous activity (settings)
                    }
                    RESULT_CANCELED -> {
                        // User canceled or entered incorrect password, continue recording
                        Log.d(
                                "PasswordActivity",
                                "Password entry canceled or incorrect, continuing recording without changes"
                        )
                        // The PasswordActivity will automatically finish itself, so we don't need
                        // to do anything here
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recording_activity)

        supportActionBar?.hide() // Hide the action bar

        // Keep this activity in focus
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        sensorDataTextView = findViewById(R.id.sensorData)

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

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
                showPasswordActivity()
            }
        }

        // Start recording immediately when the activity is created
        Log.d(TAG, "Calling startRecording from onCreate")
        startRecording()

        // Set initial visibility of buttons based on recording state
        updateButtonVisibility()

        // Set initial visibility of sensor data
        updateSensorDataVisibility()
    }

    private fun initializeManagers() {
        sensorManager = CustomSensorManager(this)
        locationManager = CustomLocationManager(this)
        bluetoothManager = CustomBluetoothManager(this)
        dataRecorder = DataRecorder(this)
        workflowManager = WorkflowManager(this, dataRecorder, sharedPreferences)
        audioRecorder = AudioRecorder(this)

        // Set up Bluetooth scan result listener
        bluetoothManager.setOnScanResultListener { devices -> scannedDevices = devices }
    }

    private fun showPasswordActivity() {
        val intent = Intent(this, PasswordActivity::class.java)
        passwordActivityLauncher.launch(intent)
    }

    private fun startRecording() {
        //        Log.d(TAG, "startRecording() called")
        val childId = sharedPreferences.getString("idChild", "000")
        val checkBoxAudioRecording = sharedPreferences.getString("checkBoxAudioRecording", "true")
        val timestamp = System.currentTimeMillis()
        val watchId = getWatchId(this)

        Log.d(TAG, "childId: $childId, checkBoxAudioRecording: $checkBoxAudioRecording")

        dataRecorder.initializeFiles(childId ?: "000", watchId, timestamp)

        // Start managers
        sensorManager.startListening()
        locationManager.startListening()
        bluetoothManager.startScanning(
                sharedPreferences.getString("frequencyRate", "1000")?.toLong() ?: 1000
        )

        //        Log.d(TAG, "checkAudioPermission() result: ${checkAudioPermission()}")
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
                    Handler(Looper.getMainLooper())
                            .postDelayed(
                                    {
                                        Log.d(TAG, "Attempting to start audio recording again")
                                        audioRecorder.startRecording()
                                    },
                                    1000
                            )
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

        // Update button visibility
        updateButtonVisibility()
    }

    private fun stopRecording() {
        Log.d(TAG, "stopRecording() called")
        isRecording = false
        sensorManager.stopListening()
        locationManager.stopListening()
        bluetoothManager.stopScanning()
        dataRecorder.closeFiles()
        workflowManager.cancelScheduledDialogs()

        val checkBoxAudioRecording = sharedPreferences.getString("checkBoxAudioRecording", "true")
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

        // Update button visibility
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        runOnUiThread {
            startButton.visibility = if (isRecording) Button.GONE else Button.VISIBLE
            stopButton.visibility = if (isRecording) Button.VISIBLE else Button.GONE
        }
    }

    private fun startUpdatingUI() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(
                object : Runnable {
                    override fun run() {
                        if (isRecording && SystemClock.elapsedRealtime() - lastUpdateTime > 1000) {
                            updateUI()
                            lastUpdateTime = SystemClock.elapsedRealtime()
                        }
                        handler.postDelayed(this, 1000)
                    }
                }
        )
    }

    private fun updateUI() {
        val timestamp = System.currentTimeMillis()
        sensorDataTextView.text =
                """
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

        val sensorDataMap =
                mapOf(
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
        val displaySensorValues =
                sharedPreferences.getString("checkBoxDisplaySensorValues", "false")?.toBoolean()
                        ?: true
        sensorDataTextView.visibility = if (displaySensorValues) View.VISIBLE else View.GONE
    }

    private fun initWorkflowQuestions() {
        //        Log.d(TAG, "initWorkflowQuestions() called")

        // Log all keys in shared preferences
        Log.d(TAG, "All keys in shared preferences:")
        sharedPreferences.all.forEach { (key, value) -> Log.d(TAG, "KEY $key: $value") }

        val selectedWorkflowName = sharedPreferences.getString("selectedWorkflow", null)
        val workflowFileContent = sharedPreferences.getString("workflowFile", null)

        Log.d(TAG, "Selected workflow name: $selectedWorkflowName")
        Log.d(TAG, "Workflow file content length: ${workflowFileContent?.length ?: 0}")

        if (selectedWorkflowName != null && workflowFileContent != null) {
            try {
                val workflow =
                        workflowManager.initializeWorkflow(
                                workflowFileContent,
                                selectedWorkflowName
                        )
                if (workflow != null) {
                    Log.d(TAG, "Workflow initialized successfully: ${workflow.workflow_name}")
                    Log.d(TAG, "Number of questions: ${workflow.questions.size}")
                    workflowManager.scheduleCustomDialogs(workflow)
                    Log.d(TAG, "Custom dialogs scheduled")
                } else {
                    Log.e(TAG, "Workflow initialization failed: Null workflow returned")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Workflow initialization failed: ${e.message}", e)
                e.printStackTrace()
            }
        } else {
            if (selectedWorkflowName == null) {
                Log.e(TAG, "Selected workflow name is null")
            }
            if (workflowFileContent == null) {
                Log.e(TAG, "Workflow file content is null")
            }
        }
    }

    private fun getWatchId(context: Context): String {
        return android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
        )
    }

    private fun checkAudioPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                        PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Requesting audio permission")
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    AUDIO_PERMISSION_REQUEST_CODE
            )
            return false
        }
        Log.d(TAG, "Audio permission already granted")
        return true
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "Audio recording permission granted")
                    audioRecorder.startRecording()
                } else {
                    Log.e(TAG, "Audio recording permission denied")
                }
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        workflowManager.cancelScheduledDialogs()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
        workflowManager.cancelScheduledDialogs()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        if (isRecording) {
            Log.d(TAG, "Recording is active, rescheduling dialogs")
            workflowManager.rescheduleDialogs()
        } else {
            Log.d(TAG, "Recording is not active, dialogs not rescheduled")
        }
    }

    companion object {
        private const val TAG = "RecordingActivity"
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1001
    }
}
