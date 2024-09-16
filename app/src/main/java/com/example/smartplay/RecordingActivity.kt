package com.example.smartplay

import AudioRecorder
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
import com.example.smartplay.sensors.CustomSensorManager
import com.example.smartplay.location.CustomLocationManager
import com.example.smartplay.bluetooth.CustomBluetoothManager
import com.example.smartplay.data.DataRecorder
import com.example.smartplay.workflow.WorkflowManager
import com.example.smartplay.utils.QuestionRecorder
//import com.example.smartplay.utils.AudioRecorder

class RecordingActivity : AppCompatActivity(), QuestionRecorder {

    private lateinit var sensorManager: CustomSensorManager
    private lateinit var locationManager: CustomLocationManager
    private lateinit var bluetoothManager: CustomBluetoothManager
    private lateinit var dataRecorder: DataRecorder
    private lateinit var workflowManager: WorkflowManager
    private lateinit var audioRecorder: AudioRecorder

    private var isRecording = false
    private var lastUpdateTime: Long = 0
    private var scannedDevices: Map<String, Int> = emptyMap()

    private val passwordActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            stopRecording()
            finish() // Navigate back to the previous activity (settings)
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
        startRecording()

        // Set initial visibility of buttons based on recording state
        startButton.visibility = if (isRecording) Button.GONE else Button.VISIBLE
        stopButton.visibility = if (isRecording) Button.VISIBLE else Button.GONE
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
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val childId = sharedPref.getString("idChild", "000")
        val checkBoxAudioRecording = sharedPref.getString("checkBoxAudioRecording", "false")
        val timestamp = System.currentTimeMillis()
        val watchId = getWatchId(this)

        dataRecorder.initializeFiles(childId ?: "000", watchId, timestamp)

        // Start managers
        sensorManager.startListening()
        locationManager.startListening()
        bluetoothManager.startScanning(sharedPref.getString("frequencyRate", "1000")?.toLong() ?: 1000)

        // Record audio
        if (checkBoxAudioRecording?.toBoolean() == true) {
            audioRecorder.startRecording()
        }

        isRecording = true

        // Initialize the workflow questions
        initWorkflowQuestions()

        // Start updating UI
        startUpdatingUI()
    }

    private fun stopRecording() {
        isRecording = false
        sensorManager.stopListening()
        locationManager.stopListening()
        bluetoothManager.stopScanning()
        dataRecorder.closeFiles()

        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val checkBoxAudioRecording = sharedPref.getString("checkBoxAudioRecording", "false")
        // Stop audio recording
        if (checkBoxAudioRecording?.toBoolean() == true) {
            audioRecorder.stopRecording()
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
        val sensorDataTextView = findViewById<TextView>(R.id.sensorData)
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
    }
}
