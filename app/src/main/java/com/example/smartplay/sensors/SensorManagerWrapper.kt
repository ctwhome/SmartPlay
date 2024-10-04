package com.example.smartplay.sensors

import android.content.Context

data class SensorData(
    val heartRate: Float,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
    val gyroX: Float,
    val gyroY: Float,
    val gyroZ: Float,
    val magnetoX: Float,
    val magnetoY: Float,
    val magnetoZ: Float,
    val steps: Int
)

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

class SensorManagerWrapper(private val context: Context) {
    private val sensorManager = CustomSensorManager(context)
    private val locationManager = CustomLocationManager(context)

    fun startSensors() {
        sensorManager.startListening()
        locationManager.startListening()
    }

    fun stopSensors() {
        sensorManager.stopListening()
        locationManager.stopListening()
    }

    fun getSensorData(): SensorData {
        return SensorData(
            heartRate = sensorManager.heartRate,
            accelX = sensorManager.accelX,
            accelY = sensorManager.accelY,
            accelZ = sensorManager.accelZ,
            gyroX = sensorManager.gyroX,
            gyroY = sensorManager.gyroY,
            gyroZ = sensorManager.gyroZ,
            magnetoX = sensorManager.magnetoX,
            magnetoY = sensorManager.magnetoY,
            magnetoZ = sensorManager.magnetoZ,
            steps = sensorManager.sessionSteps.toInt()
        )
    }

    fun getLocationData(): LocationData {
        return LocationData(
            latitude = locationManager.latitude,
            longitude = locationManager.longitude
        )
    }
}