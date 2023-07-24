package com.example.smartplay

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.math.abs




class RecordingActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private lateinit var csvWriter: FileWriter
    private var heartRateSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null
    private var isRecording = false

    private var lastUpdateTime: Long = 0
    private var latitude: Double = 0.0
    private var longitud: Double = 0.0

    private var heartRate: Float = 0F

    private var accelX: Float = 0F
    private var accelY: Float = 0F
    private var accelZ: Float = 0F

    private var gyroX: Float = 0F
    private var gyroY: Float = 0F
    private var gyroZ: Float = 0F

    private var magnetoX: Float = 0F
    private var magnetoY: Float = 0F
    private var magnetoZ: Float = 0F

    private var timestamp: Long = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recording_activity)

        supportActionBar?.hide() // Hide the action bar

        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        startButton.setOnClickListener {
            println("Start button pressed" )
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
                stopRecording()
            }
        }
    }

    private fun startRecording() {
        checkPermission()
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(dir, "sensor_data.csv")
        try {
            csvWriter = FileWriter(file, true)
            csvWriter.append("timestamp,latitude,longitude,heartRate,accelX,accelY,accelZ,gyroX,gyroY,gyroZ,magnetoX,magnetoY,magnetoZ\n")
            isRecording = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this)
    }

    private fun stopRecording() {
        isRecording = false
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
        try {
            csvWriter.flush()
            csvWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onSensorChanged(event: SensorEvent) {

        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> {
                heartRate = event.values[0]
                writeDataToCSV(timestamp, heartRate = heartRate)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                accelX = event.values[0]
                accelY = event.values[1]
                accelZ = event.values[2]
//                writeDataToCSV(timestamp, accelX = x, accelY = y, accelZ = z)
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
//                writeDataToCSV(timestamp, gyroX = x, gyroY = y, gyroZ = z)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magnetoX = event.values[0]
                magnetoY = event.values[1]
                magnetoZ = event.values[2]
            }
        }


        if (isRecording && abs(SystemClock.elapsedRealtime() - lastUpdateTime) > 1000) {

            // Write the sensor data on the screen
            val sensorData = findViewById<TextView>(R.id.sensorData)


            val timestamp = System.currentTimeMillis()
            sensorData.setText(
                "⏱️" + timestamp.toString()
                + "\n❤️ " + heartRate.toString()
                + "\n🌍 " + latitude.toString() + " " + longitud.toString()
                + "\n🧭 " + magnetoX.toString() + " " + magnetoY.toString() + " " + magnetoZ.toString()
                + "\n🔀 " + gyroX.toString() + " " + gyroY.toString() + " " + gyroZ.toString()
                + "\n🏎️ " + accelX.toString() + " " + accelY.toString() + " " + accelZ.toString()

            )
            writeDataToCSV(
                timestamp,
                latitude,
                longitud,
                heartRate,
                accelX,
                accelY,
                accelZ,
                magnetoX,
                magnetoY,
                magnetoZ,
                gyroX,
                gyroY,
                gyroZ,
            )

            lastUpdateTime = SystemClock.elapsedRealtime()

        }
    }

    private fun writeDataToCSV(
        timestamp: Long,
        latitude: Double? = null,
        longitude: Double? = null,
        heartRate: Float? = null,
        accelX: Float? = null,
        accelY: Float? = null,
        accelZ: Float? = null,
        gyroX: Float? = null,
        gyroY: Float? = null,
        gyroZ: Float? = null,
        magnetoX: Float? = null,
        magnetoY: Float? = null,
        magnetoZ: Float? = null
    ) {
        try {
            csvWriter.append("$timestamp,$latitude,$longitude,$heartRate,$accelX,$accelY,$accelZ,$gyroX,$gyroY,$gyroZ,$magnetoX,$magnetoY,$magnetoZ\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // not used
    }

    override fun onLocationChanged(location: Location) {
        if (isRecording) {
            val timestamp = System.currentTimeMillis()
            writeDataToCSV(timestamp, latitude = location.latitude, longitude = location.longitude)
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) != PackageManager.PERMISSION_GRANTED ||

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED

        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BODY_SENSORS
                ),
                0
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    // Disable the functionality that depends on this permission.
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}

