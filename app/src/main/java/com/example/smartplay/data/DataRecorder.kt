package com.example.smartplay.data

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException

class DataRecorder(private val context: Context) {
    private val TAG = "DataRecorder"
    private lateinit var sensorCsvWriter: FileWriter
    private lateinit var btCsvWriter: FileWriter
    private lateinit var questionCsvWriter: FileWriter

    fun initializeFiles(childId: String, watchId: String, timestamp: Long) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        initializeSensorFile(dir, childId, watchId, timestamp)
        initializeBluetoothFile(dir, childId, watchId, timestamp)
        initializeQuestionFile(dir, childId, watchId, timestamp)
    }

    private fun initializeSensorFile(dir: File?, childId: String, watchId: String, timestamp: Long) {
        val sensorFile = File(dir, "${childId}_SENSORS_${watchId}_$timestamp.csv")
        try {
            sensorCsvWriter = FileWriter(sensorFile, true)
            if (sensorFile.length() == 0L) {
                writeSensorHeader()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error creating sensor CSV file: ${e.message}")
        }
    }

    private fun initializeBluetoothFile(dir: File?, childId: String, watchId: String, timestamp: Long) {
        val btFile = File(dir, "${childId}_BT_${watchId}_$timestamp.csv")
        try {
            btCsvWriter = FileWriter(btFile, true)
            if (btFile.length() == 0L) {
                btCsvWriter.append("timestamp,devices\n").flush()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error creating Bluetooth CSV file: ${e.message}")
        }
    }

    private fun initializeQuestionFile(dir: File?, childId: String, watchId: String, timestamp: Long) {
        val questionFile = File(dir, "${childId}_QUESTIONS_${watchId}_$timestamp.csv")
        try {
            questionCsvWriter = FileWriter(questionFile, true)
            if (questionFile.length() == 0L) {
                questionCsvWriter.append("timestamp,questionID,questionText,answer\n").flush()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error creating question CSV file: ${e.message}")
        }
    }

    private fun writeSensorHeader() {
        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val header = StringBuilder("timestamp,")

        if (sharedPref.getString("checkBoxLatitude", "true").toBoolean()) header.append("latitude,")
        if (sharedPref.getString("checkBoxLongitude", "true").toBoolean()) header.append("longitude,")
        if (sharedPref.getString("checkBoxHeartRate", "true").toBoolean()) header.append("heartRate,")
        if (sharedPref.getString("checkBoxAccelerometer", "true").toBoolean()) header.append("accelX,accelY,accelZ,")
        if (sharedPref.getString("checkBoxGyroscope", "true").toBoolean()) header.append("gyroX,gyroY,gyroZ,")
        if (sharedPref.getString("checkBoxMagnetometer", "true").toBoolean()) header.append("magnetoX,magnetoY,magnetoZ,")
        if (sharedPref.getString("checkBoxSteps", "true").toBoolean()) header.append("steps,")

        if (header.last() == ',') header.setLength(header.length - 1)
        header.append("\n")

        sensorCsvWriter.append(header.toString()).flush()
    }

    fun writeSensorData(sensorData: Map<String, Any>) {
        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val data = StringBuilder("${sensorData["timestamp"]},")

        if (sharedPref.getString("checkBoxLatitude", "true").toBoolean()) data.append("${sensorData["latitude"]},")
        if (sharedPref.getString("checkBoxLongitude", "true").toBoolean()) data.append("${sensorData["longitude"]},")
        if (sharedPref.getString("checkBoxHeartRate", "true").toBoolean()) data.append("${sensorData["heartRate"]},")
        if (sharedPref.getString("checkBoxAccelerometer", "true").toBoolean())
            data.append("${sensorData["accelX"]},${sensorData["accelY"]},${sensorData["accelZ"]},")
        if (sharedPref.getString("checkBoxGyroscope", "true").toBoolean())
            data.append("${sensorData["gyroX"]},${sensorData["gyroY"]},${sensorData["gyroZ"]},")
        if (sharedPref.getString("checkBoxMagnetometer", "true").toBoolean())
            data.append("${sensorData["magnetoX"]},${sensorData["magnetoY"]},${sensorData["magnetoZ"]},")
        if (sharedPref.getString("checkBoxSteps", "true").toBoolean()) data.append("${sensorData["steps"]},")

        if (data.last() == ',') data.setLength(data.length - 1)
        data.append("\n")

        try {
            sensorCsvWriter.append(data.toString()).flush()
        } catch (e: IOException) {
            Log.e(TAG, "Error writing sensor data to CSV: ${e.message}")
        }
    }

    fun writeBluetoothData(timestamp: Long, devices: Map<String, Int>) {
        val data = StringBuilder("$timestamp")
        devices.forEach { (device, rssi) -> data.append(",$device-$rssi") }
        data.append("\n")

        try {
            btCsvWriter.append(data.toString()).flush()
        } catch (e: IOException) {
            Log.e(TAG, "Error writing Bluetooth data to CSV: ${e.message}")
        }
    }

    fun writeQuestionData(timestamp: Long, questionID: String, questionText: String, answer: String) {
        try {
            questionCsvWriter.append("$timestamp,$questionID,$questionText,$answer\n").flush()
        } catch (e: IOException) {
            Log.e(TAG, "Error writing question data to CSV: ${e.message}")
        }
    }

    fun closeFiles() {
        try {
            sensorCsvWriter.close()
            btCsvWriter.close()
            questionCsvWriter.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing CSV files: ${e.message}")
        }
    }
}