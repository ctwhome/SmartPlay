package com.example.smartplay.recording

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.example.smartplay.sensors.SensorManagerWrapper
import com.example.smartplay.workflow.WorkflowHandler
import com.example.smartplay.sensors.AudioRecorderManager
import com.example.smartplay.sensors.BluetoothManagerWrapper
import com.example.smartplay.workflow.Workflow

class RecordingManager(
    private val context: Context,
    private val sensorManager: SensorManagerWrapper,
    private val bluetoothManager: BluetoothManagerWrapper,
    private val audioRecorder: AudioRecorderManager,
    private val workflowHandler: WorkflowHandler
) {
    private var isRecording = false
    private var dataRecorder: DataRecorder? = null
    private var selectedWorkflow: Workflow? = null
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

    fun startRecording() {
        if (!isRecording) {
            initializeDataRecorder()
            sensorManager.startSensors()
            bluetoothManager.startScanning()
            audioRecorder.startRecording()
            // Removed the call to workflowHandler.initializeWorkflow() here
            isRecording = true
        }
    }

    fun stopRecording() {
        if (isRecording) {
            sensorManager.stopSensors()
            bluetoothManager.stopScanning()
            audioRecorder.stopRecording()
            workflowHandler.stopWorkflow()
            dataRecorder?.closeFiles()
            dataRecorder = null
            isRecording = false
        }
    }

    fun isRecording(): Boolean = isRecording

    private fun initializeDataRecorder() {
        val childId = getChildId()
        val watchId = getWatchId()
        val timestamp = System.currentTimeMillis()

        dataRecorder = DataRecorder(context)
        dataRecorder?.initializeFiles(childId, watchId, timestamp)
    }

    fun recordSensorData(sensorData: Map<String, Any>) {
        dataRecorder?.writeSensorData(sensorData)
    }

    fun recordBluetoothData(timestamp: Long, devices: Map<String, Int>) {
        dataRecorder?.writeBluetoothData(timestamp, devices)
    }

    fun writeQuestionData(timestamp: Long, questionId: String, questionTitle: String, answer: String) {
        dataRecorder?.writeQuestionData(timestamp, questionId, questionTitle, answer)
    }

    fun initializeWorkflow(workflowFileContent: String, selectedWorkflowName: String): Workflow? {
        selectedWorkflow = workflowHandler.initializeWorkflow(workflowFileContent, selectedWorkflowName)
        return selectedWorkflow
    }

    fun getDataRecorder(): DataRecorder? = dataRecorder

    private fun getChildId(): String {
        return sharedPreferences.getString("idChild", "000") ?: "000"
    }

    private fun getWatchId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}