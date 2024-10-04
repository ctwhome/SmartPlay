package com.example.smartplay

import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import com.example.smartplay.recording.RecordingManager
import com.example.smartplay.workflow.WorkflowService
import com.example.smartplay.workflow.Workflow
import com.example.smartplay.recording.QuestionRecorder

// Import new component classes
import com.example.smartplay.sensors.AudioRecorderManager
import com.example.smartplay.sensors.BluetoothManagerWrapper
import com.example.smartplay.sensors.SensorManagerWrapper
import com.example.smartplay.workflow.WorkflowManager

class RecordingActivity : AppCompatActivity(), QuestionRecorder {
    private lateinit var recordingManager: RecordingManager
    private lateinit var sensorManagerWrapper: SensorManagerWrapper
    private lateinit var bluetoothManagerWrapper: BluetoothManagerWrapper
    private lateinit var audioRecorderManager: AudioRecorderManager
    private lateinit var workflowManager: WorkflowManager
    private lateinit var sensorDataTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    private var lastUpdateTime: Long = 0
    private var scannedDevices: Map<String, Int> = emptyMap()
    private var selectedWorkflow: Workflow? = null

    private val passwordActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    recordingManager.stopRecording()
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

        // Set the static reference to this instance
        currentInstance = this

        supportActionBar?.hide() // Hide the action bar

        // Keep this activity in focus
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        sensorDataTextView = findViewById(R.id.sensorData)

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        initializeManagers()

        startButton.setOnClickListener {
            if (!recordingManager.isRecording()) {
                startButton.visibility = Button.GONE
                stopButton.visibility = Button.VISIBLE
                recordingManager.startRecording()
            }
        }

        stopButton.setOnClickListener {
            if (recordingManager.isRecording()) {
                showPasswordActivity()
            }
        }

        // Start recording immediately when the activity is created
        Log.d(TAG, "Calling startRecording from onCreate")
        recordingManager.startRecording()

        // Set initial visibility of buttons based on recording state
        updateButtonVisibility()

        // Set initial visibility of sensor data
        updateSensorDataVisibility()

        // Initialize workflow questions
        initWorkflowQuestions()

        // Start updating UI
        startUpdatingUI()
    }

    private fun initializeManagers() {
        Log.d(TAG, "Initializing managers")
        sensorManagerWrapper = SensorManagerWrapper(this)
        bluetoothManagerWrapper = BluetoothManagerWrapper(this)
        audioRecorderManager = AudioRecorderManager(this)
        workflowManager = WorkflowManager(this)

        // Initialize RecordingManager
        recordingManager = RecordingManager(
            context = this,
            sensorManager = sensorManagerWrapper,
            bluetoothManager = bluetoothManagerWrapper,
            audioRecorder = audioRecorderManager,
            workflowManager = workflowManager
        )

        // Set up Bluetooth scan result listener
        bluetoothManagerWrapper.setOnScanResultListener { devices -> scannedDevices = devices }
        Log.d(TAG, "Managers initialized")
    }

    private fun showPasswordActivity() {
        Log.d(TAG, "Showing password activity")
        val intent = Intent(this, PasswordActivity::class.java)
        passwordActivityLauncher.launch(intent)
    }

    private fun navigateToSettings() {
        Log.d(TAG, "Navigating to SettingsActivity")
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateButtonVisibility() {
        runOnUiThread {
            startButton.visibility = if (recordingManager.isRecording()) Button.GONE else Button.VISIBLE
            stopButton.visibility = if (recordingManager.isRecording()) Button.VISIBLE else Button.GONE
        }
    }

    private fun startUpdatingUI() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (recordingManager.isRecording() && SystemClock.elapsedRealtime() - lastUpdateTime > 1000) {
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
        val sensorData = sensorManagerWrapper.getSensorData()
        val locationData = sensorManagerWrapper.getLocationData()

        sensorDataTextView.text = """
        ⏱️ $timestamp
        ❤️ ${sensorData.heartRate}
        🌍 ${locationData.latitude} ${locationData.longitude}
        🧭 ${sensorData.magnetoX} ${sensorData.magnetoY} ${sensorData.magnetoZ}
        🔀 ${sensorData.gyroX} ${sensorData.gyroY} ${sensorData.gyroZ}
        🏎️ ${sensorData.accelX} ${sensorData.accelY} ${sensorData.accelZ}
        👣 ${sensorData.steps}
        📡 $scannedDevices
        🎙️ ${if (audioRecorderManager.isRecording()) "Recording" else "Not Recording"}
        """.trimIndent()

        val sensorDataMap = mapOf(
            "timestamp" to timestamp,
            "latitude" to locationData.latitude,
            "longitude" to locationData.longitude,
            "heartRate" to sensorData.heartRate,
            "accelX" to sensorData.accelX,
            "accelY" to sensorData.accelY,
            "accelZ" to sensorData.accelZ,
            "gyroX" to sensorData.gyroX,
            "gyroY" to sensorData.gyroY,
            "gyroZ" to sensorData.gyroZ,
            "magnetoX" to sensorData.magnetoX,
            "magnetoY" to sensorData.magnetoY,
            "magnetoZ" to sensorData.magnetoZ,
            "steps" to sensorData.steps
        )

        recordingManager.recordSensorData(sensorDataMap)
        recordingManager.recordBluetoothData(timestamp, scannedDevices)
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
                Log.d(TAG, "Initializing workflow with RecordingManager")
                selectedWorkflow = recordingManager.initializeWorkflow(workflowFileContent, selectedWorkflowName)
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Audio recording permission granted")
                    audioRecorderManager.startRecording()
                } else {
                    Log.e(TAG, "Audio recording permission denied")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        if (recordingManager.isRecording()) {
            recordingManager.stopRecording()
        }
        // Clear the static reference when the activity is destroyed
        currentInstance = null
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
        recordingManager.writeQuestionData(timestamp, questionId, questionTitle, answer)
        Log.d(TAG, "Data written to CSV: $timestamp, $questionId, $questionTitle, $answer")
    }

    fun recordQuestionAsked(timestamp: Long, questionId: String, questionTitle: String) {
        writeQuestionsToCSV(timestamp, questionId, questionTitle, "ASKED")
        Log.d(TAG, "Question asked: $questionId, $questionTitle")
    }

    fun recordQuestionAnswered(timestamp: Long, questionId: String, questionTitle: String, answer: String) {
        writeQuestionsToCSV(timestamp, questionId, questionTitle, answer)
        Log.d(TAG, "Question answered: $questionId, $questionTitle, $answer")
    }

    companion object {
        private const val TAG = "RecordingActivity"
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1001

        // Static reference to the current RecordingActivity instance
        private var currentInstance: RecordingActivity? = null

        @JvmStatic
        fun recordQuestionAskedStatic(timestamp: Long, questionId: String, questionTitle: String) {
            currentInstance?.recordQuestionAsked(timestamp, questionId, questionTitle)
                ?: Log.e(TAG, "Unable to record question asked: RecordingActivity instance is null")
        }

        @JvmStatic
        fun recordQuestionAnsweredStatic(timestamp: Long, questionId: String, questionTitle: String, answer: String) {
            currentInstance?.recordQuestionAnswered(timestamp, questionId, questionTitle, answer)
                ?: Log.e(TAG, "Unable to record question answered: RecordingActivity instance is null")
        }
    }
}
