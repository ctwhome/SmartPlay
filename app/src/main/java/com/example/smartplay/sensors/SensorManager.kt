package com.example.smartplay.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class CustomSensorManager(context: Context) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var heartRateSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null

    var heartRate: Float = 0F
    var accelX: Float = 0F
    var accelY: Float = 0F
    var accelZ: Float = 0F
    var gyroX: Float = 0F
    var gyroY: Float = 0F
    var gyroZ: Float = 0F
    var magnetoX: Float = 0F
    var magnetoY: Float = 0F
    var magnetoZ: Float = 0F
    var totalSteps: Float = 0F
    var previousTotalSteps: Float = 0F
    var sessionSteps: Float = 0F

    init {
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    fun startListening() {
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Reset step counting variables
        totalSteps = 0f
        previousTotalSteps = 0f
        sessionSteps = 0f
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> heartRate = event.values[0]
            Sensor.TYPE_ACCELEROMETER -> {
                accelX = event.values[0]
                accelY = event.values[1]
                accelZ = event.values[2]
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magnetoX = event.values[0]
                magnetoY = event.values[1]
                magnetoZ = event.values[2]
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val currentTotalSteps = event.values[0]
                if (totalSteps == 0f) {
                    totalSteps = currentTotalSteps
                    previousTotalSteps = totalSteps
                } else {
                    val stepsSinceLastReading = currentTotalSteps - totalSteps
                    sessionSteps += stepsSinceLastReading
                    totalSteps = currentTotalSteps
                }
                Log.d("CustomSensorManager", "Session steps: $sessionSteps")
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                sessionSteps++
                Log.d("CustomSensorManager", "Step detected! Session steps: $sessionSteps")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }
}