package com.example.smartplay

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
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
import android.util.Log

import android.provider.Settings


import android.app.AlertDialog
import android.content.DialogInterface
import android.view.WindowManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import android.os.Handler
import android.os.Looper

data class Question(
    val question_id: Int,
    val question_title: String,
    val answers: List<String>,
    val frequency: Int? = null,
    val time_after_start_in_minutes: Int? = null,
    val time_between_repetitions_in_minutes: Int? = null
)

data class Workflow(
    val workflow_name: String,
    val questions: List<Question>
)

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
    private var longitude: Double = 0.0

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

    private val locationListener: LocationListener = LocationListener { location ->
        latitude = location.latitude
        longitude = location.longitude
    }

    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recording_activity)

        supportActionBar?.hide() // Hide the action bar


        // Keep this activity in focus
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        // Initialize the sensors and location
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0
            )
            Log.d(TAG, "No permission")
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0L, 0f, locationListener
            )
            Log.d(TAG, "Permission granted")
        }

        startButton.setOnClickListener {
            println("Start button pressed")
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

    //
    // Function to initialize the workflow questions
    //
    private fun initWorkflowQuestions() {
        val sharedData = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val workflowString = sharedData.getString("workflowFile", "")
        // cast the json file to a Workflow object

        val gson = Gson()
        val workflowListType = object : TypeToken<List<Workflow>>() {}.type
        val workflows: List<Workflow> = gson.fromJson(workflowString, workflowListType)
        val selectedWorkflowName = sharedData.getString("selectedWorkflow", "NOT_FOUND")

        Log.d(TAG, "Selected Workflow Name: $selectedWorkflowName")

        val workflow = workflows.filter { it.workflow_name.trim() == selectedWorkflowName?.trim() }
        // parse the json file workflowFile

        Log.d(TAG, "workflow!!!!!!!!!!!!!!!!!!: $workflow")

        scheduleCustomDialogs(workflow)


        // I might not need this after the previous implementation
        /*val questions = workflow[0].questions.map {
            Log.d(TAG, "Question: ${it.question_title}")
            Log.d(TAG, "Answers: ${it.answers}")
            Log.d(TAG, "Time after start in minutes: ${it.time_after_start_in_minutes}")
            Log.d(TAG, "frequency: ${it.frequency}")
            Log.d(TAG, "Time after start in minutes: ${it.time_between_repetitions_in_minutes}")

            if (it.time_after_start_in_minutes != null) {
                Log.d(TAG, "Time after start in minutes: ${it.time_after_start_in_minutes}")
                Thread {

                    //
                    // !CONTINUE HERE
                    // MAKE THE DIALOG NICE AND PRETTY and STORE THE ANSWERS IN THE CSV FILE
                    // DO NOT FORGET TO CHANGE THE TIME TO MINUTES MULTIPLY BY 60
                    //

//                Thread.sleep(it.time_after_start_in_minutes.toLong() * 60 * 1000) // multiply by 60 to convert to minutes
                    Thread.sleep(it.time_after_start_in_minutes.toLong() * 1000)
                    Log.d(TAG, "Question: ${it.question_title} => ${it.answers}")

                    // Ensure UI updates are done on the Main Thread
                    runOnUiThread {
                        showMessageDialog(this, it.question_title, it.answers)
                    }

                }.start()
            }
        }*/


    }

    val TAG = "CustomDialogScheduler"

    fun scheduleCustomDialogs(workflow: List<Workflow>) {
        workflow[0].questions.forEach { question ->
            Log.d(TAG, "Question: ${question.question_title}")
            Log.d(TAG, "Answers: ${question.answers}")
            Log.d(TAG, "Time after start in minutes: ${question.time_after_start_in_minutes}")
            Log.d(TAG, "Frequency: ${question.frequency}")
            Log.d(TAG, "Time between repetitions in minutes: ${question.time_between_repetitions_in_minutes}")

            question.time_after_start_in_minutes?.let { startTime ->
                val initialDelay = startTime * 60 * 1000L // Convert to milliseconds
                val handler = Handler(Looper.getMainLooper())

                // Schedule the initial dialog
                handler.postDelayed({
                    showMessageDialog(this, question.question_title, question.answers)
                    scheduleRepetitions(handler, question)
                }, initialDelay)
            }
        }
    }

    fun scheduleRepetitions(handler: Handler, question: Question) {
        question.frequency?.let { frequency ->
            // convert to minutes by multiplying by 60
//
//
//            CONTINUE HERE PROVIDE THE NULL WITH A DEFAULT NAME SO I CAN MAKE A MULTIPLICATION OF IT
//
//


//            val repetitionInterval = question.time_between_repetitions_in_minutes * 60 * 1000L // Convert to milliseconds
            val repetitionInterval = question.time_between_repetitions_in_minutes * 1000L // Convert to milliseconds
            for (i in 1 until frequency) {
                handler.postDelayed({
                    showMessageDialog(this, question.question_title, question.answers)
//                    showMessageDialog(question.question_title, question.answers)
                }, i * repetitionInterval)
            }
        }
    }

//    fun showMessageDialog(title: String, answers: List<String>) {
//        // Implementation of your custom dialog
//        // Example:
//        val context = // Get your context here
//        val dialog = AlertDialog.Builder(context)
//            .setTitle(title)
//            .setItems(answers.toTypedArray()) { dialog, which ->
//                // Handle item click
//            }
//            .create()
//        dialog.show()
//    }


    // END OF WORKFLOWS

    fun getWatchId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun startRecording() {
        checkPermission()

        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val childId = sharedPref.getString("idChild", "000")

//      val childId = "001"
        // Getting data from the setting page
//        val childId = intent.getStringExtra("idChild")
        // console log childId
        println("childId: $childId")

        val timestamp = System.currentTimeMillis()
        val watchId = getWatchId(this)
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(dir, childId + "_" + watchId + "_" + timestamp + ".csv")
        try {
            csvWriter = FileWriter(file, true)

            // If file is empty, write the header columns for the CSV file
            if (file.length() == 0L) {
                csvWriter.append("timestamp,latitude,longitude,heartRate,accelX,accelY,accelZ,gyroX,gyroY,gyroZ,magnetoX,magnetoY,magnetoZ\n")
            }
            isRecording = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)

        val provider = LocationManager.GPS_PROVIDER // or LocationManager.NETWORK_PROVIDER
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(provider, 1000, 1f, this)
        }


        // Initialize the workflow questions
        initWorkflowQuestions()

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
//                writeDataToCSV(timestamp, heartRate = heartRate)
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
                "⏱️" + timestamp.toString() + "\n❤️ " + heartRate.toString() + "\n🌍 " + latitude.toString() + " " + longitude.toString() + "\n🧭 " + magnetoX.toString() + " " + magnetoY.toString() + " " + magnetoZ.toString() + "\n🔀 " + gyroX.toString() + " " + gyroY.toString() + " " + gyroZ.toString() + "\n🏎️ " + accelX.toString() + " " + accelY.toString() + " " + accelZ.toString()
            )
            writeDataToCSV(
                timestamp,
                latitude,
                longitude,
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
            latitude = location.latitude
            longitude = location.longitude

//            val timestamp = System.currentTimeMillis()
//            writeDataToCSV(timestamp, latitude = location.latitude, longitude = location.longitude)
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||

            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BODY_SENSORS
            ) != PackageManager.PERMISSION_GRANTED ||

            ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED

        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS
                ), 0
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isRecording) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Recording in progress. Do you really want to leave?")
                .setCancelable(false).setPositiveButton("Yes") { _, _ -> super.onBackPressed() }
                .setNegativeButton("No", null)
            val alert = builder.create()
            alert.show()
        } else {
            super.onBackPressed()
        }
    }


    // Dialog for the notifications messages, but get the
    private fun showMessageDialog(context: Context, question: String, answers: List<String>) {
        // Check and log if the answers are empty
        if (answers.isEmpty()) {
            Log.d(TAG, "No answers provided to the dialog")
            return
        }

        // Logging answers to debug them
        Log.d(TAG, "Answers: $answers")

        // Creating an AlertDialog builder
        val builder = AlertDialog.Builder(context)

        // Set the title of the dialog to the question
        builder.setTitle(question)

        // Optional: Remove this setMessage if you want only the list visible
        // builder.setMessage("Select an answer")


        // MAKE A CUSTOM LAYOUT TO DISPLAY THE QUESTIONS


        // Use setSingleChoiceItems for a list of selectable items
        builder.setSingleChoiceItems(answers.toTypedArray(), -1) { dialog, index ->
            // Log the selected answer
            Log.d(TAG, "Selected answer: ${answers[index]}")

            // CONTINUEEEEEEEE
            // REGISTER THE ACTION HERE
            // WRITE THE ANSWER TO THE CSV FILE

            // close the dialog
            dialog.dismiss()
            // Here you can handle the answer selection
        }

        // Set the positive button action
        builder.setPositiveButton("OK") { dialog, which ->
            // Handle the OK button
        }

        // Set the negative button action
        builder.setNegativeButton("Cancel") { dialog, which ->
            // Handle the Cancel button
        }

        // Create the AlertDialog
        val dialog: AlertDialog = builder.create()

        // Show the AlertDialog
        dialog.show()
    }
}

