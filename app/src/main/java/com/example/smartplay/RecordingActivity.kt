package com.example.smartplay

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartplay.sensors.CustomBluetoothManager
import com.example.smartplay.data.DataRecorder
import com.example.smartplay.sensors.CustomLocationManager
import com.example.smartplay.sensors.CustomSensorManager
import com.example.smartplay.data.AudioRecorder
import com.example.smartplay.workflow.WorkflowManager
import com.example.smartplay.workflow.WorkflowService
import com.example.smartplay.workflow.Workflow
import com.example.smartplay.workflow.QuestionRecorder

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
    private var isRecordingSessionActive = false
    private var lastUpdateTime: Long = 0
    private var scannedDevices: Map<String, Int> = emptyMap()
    private var selectedWorkflow: Workflow? = null

    private val passwordActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    stopRecording()
                    navigateToSettings()
                }

                RESULT_CANCELED -> {
                    Log.d(TAG, "Password entry canceled or incorrect, continuing recording without changes")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
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
        Log.d(TAG, "Initializing managers")
        sensorManager = CustomSensorManager(this)
        locationManager = CustomLocationManager(this)
        bluetoothManager = CustomBluetoothManager(this)
        dataRecorder = DataRecorder(this)
        workflowManager = WorkflowManager(this, dataRecorder)
        audioRecorder = AudioRecorder(this)

        // Set up Bluetooth scan result listener
        bluetoothManager.setOnScanResultListener { devices -> scannedDevices = devices }
        Log.d(TAG, "Managers initialized")
    }

    private fun showPasswordActivity() {
        Log.d(TAG, "Showing password activity")
        val intent = Intent(this, PasswordActivity::class.java)
        passwordActivityLauncher.launch(intent)
    }

    private fun startRecording() {
        Log.d(TAG, "startRecording() called")
        if (!isRecordingSessionActive) {
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
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                Log.d(TAG, "Attempting to start audio recording again")
                                audioRecorder.startRecording()
                            }, 1000
                        )
                    }
                } else {
                    Log.e(TAG, "Audio recording permission not granted")
                }
            } else {
                Log.d(TAG, "Audio recording is disabled in preferences")
            }

            isRecording = true
            isRecordingSessionActive = true

            // Initialize the workflow questions
            initWorkflowQuestions()

            // Start updating UI
            startUpdatingUI()

            // Update sensor data visibility
            updateSensorDataVisibility()

            // Update button visibility
            updateButtonVisibility()

            Log.d(TAG, "Recording started successfully")
        }
    }

    private fun stopRecording() {
        Log.d(TAG, "stopRecording() called")
        if (isRecordingSessionActive) {
            isRecording = false
            isRecordingSessionActive = false
            sensorManager.stopListening()
            locationManager.stopListening()
            bluetoothManager.stopScanning()
            dataRecorder.closeFiles()

            // Stop audio recording
            val checkBoxAudioRecording = sharedPreferences.getString("checkBoxAudioRecording", "true")
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

            // Cancel scheduled alarms and stop the WorkflowService
            if (selectedWorkflow != null) {
                Log.d(TAG, "Cancelling alarms for workflow: ${selectedWorkflow!!.workflow_name}")
                WorkflowService.cancelAlarms(this, selectedWorkflow!!)
            } else {
                Log.e(TAG, "Cannot cancel alarms: selectedWorkflow is null")
            }
            val serviceIntent = Intent(this, WorkflowService::class.java)
            stopService(serviceIntent)
            Log.d(TAG, "WorkflowService stopped")

            // Cancel any displayed notifications
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            Log.d(TAG, "All notifications cancelled")

            // Update button visibility
            updateButtonVisibility()

            // Notify user
            Toast.makeText(this, "Recording stopped and scheduler canceled.", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Recording stopped successfully")
        }
    }

    private fun navigateToSettings() {
        Log.d(TAG, "Navigating to SettingsActivity")
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateButtonVisibility() {
        runOnUiThread {
            startButton.visibility = if (isRecording) Button.GONE else Button.VISIBLE
            stopButton.visibility = if (isRecording) Button.VISIBLE else Button.GONE
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

    @SuppressLint("SetTextI18n")
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
        📡 $scannedDevices
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
        val displaySensorValues =
            sharedPreferences.getString("checkBoxDisplaySensorValues", "false")?.toBoolean() ?: true
        sensorDataTextView.visibility = if (displaySensorValues) View.VISIBLE else View.GONE
    }

    private fun initWorkflowQuestions() {
        Log.d(TAG, "initWorkflowQuestions() called")

        val selectedWorkflowName = sharedPreferences.getString("selectedWorkflow", null)
        val workflowFileContent = sharedPreferences.getString("workflowFile", null)

        Log.d(TAG, "Selected workflow name: $selectedWorkflowName")
        Log.d(TAG, "Workflow file content length: ${workflowFileContent?.length ?: 0}")

        if (selectedWorkflowName != null && workflowFileContent != null) {
            try {
                Log.d(TAG, "Initializing workflow with WorkflowManager")
                selectedWorkflow = workflowManager.initializeWorkflow(workflowFileContent, selectedWorkflowName)
                if (selectedWorkflow != null) {
                    Log.d(TAG, "Workflow initialized successfully: ${selectedWorkflow!!.workflow_name}")
                    Log.d(TAG, "Number of questions: ${selectedWorkflow!!.questions.size}")

                    // Start the WorkflowService
                    startWorkflowService()
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

    private fun startWorkflowService() {
        Log.d(TAG, "startWorkflowService() called")
        if (selectedWorkflow != null) {
            val serviceIntent = Intent(this, WorkflowService::class.java).apply {
                putExtra("workflow", selectedWorkflow)
            }
            Log.d(TAG, "Starting WorkflowService as a foreground service")
            ContextCompat.startForegroundService(this, serviceIntent)
            Log.d(TAG, "WorkflowService started")
        } else {
            Log.e(TAG, "Cannot start WorkflowService: selectedWorkflow is null")
        }
    }

    @SuppressLint("HardwareIds")
    private fun getWatchId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ANDROID_ID
        )
    }

    private fun checkAudioPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Requesting audio permission")
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_REQUEST_CODE
            )
            return false
        }
        Log.d(TAG, "Audio permission already granted")
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        if (isRecording) {
            stopRecording()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun writeQuestionsToCSV(
        timestamp: Long,
        questionId: String,
        questionTitle: String,
        answer: String
    ) {
        if (isRecordingSessionActive) {
            dataRecorder.writeQuestionData(timestamp, questionId, questionTitle, answer)
            Log.d(TAG, "Data written to CSV: $timestamp, $questionId, $questionTitle, $answer")
        } else {
            Log.w(TAG, "Attempted to write data when recording session is not active")
        }
    }

    fun recordQuestionAsked(timestamp: Long, questionId: String, questionTitle: String) {
        if (isRecordingSessionActive) {
            writeQuestionsToCSV(timestamp, questionId, questionTitle, "ASKED")
            Log.d(TAG, "Question asked: $questionId, $questionTitle")
        } else {
            Log.w(TAG, "Attempted to record question asked when recording session is not active")
        }
    }

    fun recordQuestionAnswered(timestamp: Long, questionId: String, questionTitle: String, answer: String) {
        if (isRecordingSessionActive) {
            writeQuestionsToCSV(timestamp, questionId, questionTitle, answer)
            Log.d(TAG, "Question answered: $questionId, $questionTitle, $answer")
        } else {
            Log.w(TAG, "Attempted to record question answered when recording session is not active")
        }
    }

    companion object {
        private const val TAG = "RecordingActivity"
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1001

        @JvmStatic
        fun recordQuestionAnsweredStatic(context: Context, timestamp: Long, questionId: String, questionTitle: String, answer: String) {
            val activity = context.applicationContext
            if (activity is RecordingActivity) {
                activity.recordQuestionAnswered(timestamp, questionId, questionTitle, answer)
            } else {
                Log.e(TAG, "Unable to record question answer: context is not RecordingActivity")
            }
        }
    }
}
