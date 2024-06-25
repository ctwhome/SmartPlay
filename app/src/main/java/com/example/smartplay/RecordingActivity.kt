package com.example.smartplay

import AudioRecorder
import android.Manifest
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
import android.view.WindowManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.example.smartplay.utils.Workflow
import com.example.smartplay.utils.scheduleCustomDialogs
import com.example.smartplay.utils.stopAllNotifications


// Bluetooth
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.view.GestureDetector

class RecordingActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

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

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val scannedDevices = mutableMapOf<String, Int>()
    private lateinit var scanHandler: Handler
    private lateinit var scanRunnable: Runnable

    private lateinit var audioRecorder: AudioRecorder

    private lateinit var csvWriter: FileWriter
    private lateinit var csvQuestionWriter: FileWriter
    private lateinit var csvBTWriter: FileWriter

    // Steps counting
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    private lateinit var gestureDetector: GestureDetector

    companion object {
        private lateinit var csvQuestionWriter: FileWriter

        fun writeQuestionsToCSV(
            timestamp: Long, questionID: String, questionText: String, answer: String
        ) {
            try {
                csvQuestionWriter?.append("$timestamp,$questionID,$questionText,$answer\n")
                csvQuestionWriter?.flush() // Ensure data is written to the file
                Log.d(TAG, "Question logged: $timestamp,$questionID,$questionText,$answer")
            } catch (e: IOException) {
                Log.e(TAG, "Error writing to CSV: ${e.message}")
                e.printStackTrace()
            }
        }

        fun setCSVWriter(writer: FileWriter) {
            csvQuestionWriter = writer
        }
    }

    private val locationListener: LocationListener = LocationListener { location ->
        latitude = location.latitude
        longitude = location.longitude
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recording_activity)

        supportActionBar?.hide() // Hide the action bar

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)


        // Keep this activity in focus   // TODO
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Check permissions and start scanning
        checkPermissions()



        audioRecorder = AudioRecorder(this)
        // Initialize the sensors and location
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

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


        // Initialize Bluetooth
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner


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
                // open password dialog
                showExitDialog()
            }
        }


        // Start recording immediately when the activity is created
        startRecording()

        // Set initial visibility of buttons based on recording state
        startButton.visibility = if (isRecording) Button.GONE else Button.VISIBLE
        stopButton.visibility = if (isRecording) Button.VISIBLE else Button.GONE
        // Set secure flag to prevent recent apps view


        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
        )

        // Start the foreground service
        val serviceIntent = Intent(this, ForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    // permisions required higher versions
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
//                    Manifest.permission.BODY_SENSORS_BACKGROUND,

                ), 0
            )
        } else {
            startScanning()
        }
    }

    private fun startScanning(context: Context = this) {

        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val frequencyRate = sharedPref.getString("frequencyRate", "1000")?.toLong() ?: 1000

        scanHandler = Handler(Looper.getMainLooper())
        scanRunnable = object : Runnable {
            override fun run() {
                bluetoothLeScanner.startScan(null, ScanSettings.Builder().build(), scanCallback)
                scanHandler.postDelayed({
                    bluetoothLeScanner.stopScan(scanCallback)
                    recordBtData()
                    scanHandler.postDelayed(this, frequencyRate.toLong())
                }, frequencyRate.toLong())
            }
        }
        scanHandler.post(scanRunnable)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceAddress = result.device.address
            val rssi = result.rssi
            scannedDevices[deviceAddress] = rssi
        }
    }

    private fun recordBtData() {
        val timestamp = System.currentTimeMillis()
        val data = StringBuilder("$timestamp")

        scannedDevices.forEach { (device, rssi) ->
            data.append(",$device-$rssi")
        }

        try {
            csvBTWriter.append(data.toString()).append("\n")
            csvBTWriter.flush()
            Log.d(TAG, "Bluetooth data recorded: $data")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Clear scanned devices for the next interval
        scannedDevices.clear()
    }

    //
    // Function to initialize the workflow questions
    //
    private fun initWorkflowQuestions() {
        val sharedData = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val workflowString = sharedData.getString("workflowFile", "")

        // if no workflows file  or empty, return
        if (workflowString.isNullOrEmpty()) {
            return
        }

        // cast the json file to a Workflow object

        val gson = Gson()
        val workflowListType = object : TypeToken<List<Workflow>>() {}.type
        val workflows: List<Workflow> = gson.fromJson(workflowString, workflowListType)
        val selectedWorkflowName = sharedData.getString("selectedWorkflow", "NOT_FOUND")

        Log.d(TAG, "Selected Workflow Name: $selectedWorkflowName")

        if (workflows.isNotEmpty()) {
            // Access the first element: myList[0]
            val workflow =
                workflows.filter { it.workflow_name.trim() == selectedWorkflowName?.trim() }
            // parse the json file workflowFile
            scheduleCustomDialogs(workflow, this)
        } else {
            // Handle the case where the list is empty
            return
        }
    }

    fun getWatchId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun startRecording() {
//        checkPermission()



        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val childId = sharedPref.getString("idChild", "000")
        val checkBoxAudioRecording = sharedPref.getString("checkBoxAudioRecording", "true")
        val timestamp = System.currentTimeMillis()
        val watchId = getWatchId(this)
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val btFile = File(dir, childId + "_BT_" + watchId + "_" + timestamp + ".csv")

        try {
            csvBTWriter = FileWriter(btFile, true)
            // Write header
            if (btFile.length() == 0L) {
                csvBTWriter.append("timestamp").append("\n").flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Start Bluetooth scanning
        startScanning()

        // Record audio
        if (checkBoxAudioRecording.toBoolean()) {
            audioRecorder.startRecording()
        }

        val file = File(dir, childId + "_SENSORS_" + watchId + "_" + timestamp + ".csv")
        val questionFile = File(dir, childId + "_QUESTIONS_" + watchId + "_" + timestamp + ".csv")
        try {
            csvWriter = FileWriter(file, true)
            csvQuestionWriter = FileWriter(questionFile, true)
            RecordingActivity.setCSVWriter(csvQuestionWriter)

            // If file is empty, write the header columns for the CSV file
            if (file.length() == 0L) {
                // Conditional statements to determine which sensors to record
                val header = StringBuilder("timestamp,")
                val writeLatitude = sharedPref.getString("checkBoxLatitude", "true")
                val writeLongitude = sharedPref.getString("checkBoxLongitude", "true")
                val writeHeartRate = sharedPref.getString("checkBoxHeartRate", "true")
                val writeAccelerometer = sharedPref.getString("checkBoxAccelerometer", "true")
                val writeGyroscope = sharedPref.getString("checkBoxGyroscope", "true")
                val writeMagnetometer = sharedPref.getString("checkBoxMagnetometer", "true")
                val writeSteps = sharedPref.getString("checkBoxSteps", "true")

                if (writeLatitude.toBoolean()) header.append("latitude,")
                if (writeLongitude.toBoolean()) header.append("longitude,")
                if (writeHeartRate.toBoolean()) header.append("heartRate,")
                if (writeAccelerometer.toBoolean()) header.append("accelX,accelY,accelZ,")
                if (writeGyroscope.toBoolean()) header.append("gyroX,gyroY,gyroZ,")
                if (writeMagnetometer.toBoolean()) header.append("magnetoX,magnetoY,magnetoZ,")
                if (writeSteps.toBoolean()) header.append("steps,")

                // Remove trailing comma and add newline
                if (header.last() == ',') header.setLength(header.length - 1)
                header.append("\n")

                csvWriter.append(header.toString())
            }
            if (questionFile.length() == 0L) {
                csvQuestionWriter.append("timestamp,questionID,questionText,answer\n")
            }
            isRecording = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        // Register step counter and step detector sensors
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)

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
        stopAllNotifications()
        try {
            csvWriter.flush()
            csvWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        scanHandler.removeCallbacks(scanRunnable)
        try {
            csvBTWriter.flush()
            csvBTWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val checkBoxAudioRecording = sharedPref.getString("checkBoxAudioRecording", "false")
        // Stop audio recording
        if (checkBoxAudioRecording.toBoolean()) {

            audioRecorder.stopRecording()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {

        // Uncomment to
        // UPDATE STATS IN THE RECORDING SCREEN
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

            Sensor.TYPE_STEP_COUNTER -> {
                val steps = event.values[0]
                val currentSteps = steps + previousTotalSteps
                previousTotalSteps = steps
                totalSteps = currentSteps
//                Log.d(TAG, "Total steps: $totalSteps")
            }
//            Sensor.TYPE_STEP_DETECTOR -> {
//                Log.d(TAG, "Step detected!")
//            }
        }


        if (isRecording && abs(SystemClock.elapsedRealtime() - lastUpdateTime) > 1000) { // TODO is this the frequency? the 1000
            val timestamp = System.currentTimeMillis()

            // Write the sensor data on the screen
            // Uncomment to
            /*
            val sensorData = findViewById<TextView>(R.id.sensorData)
            sensorData.text = """
            ⏱️$timestamp
            ❤️ $heartRate
            🌍 $latitude $longitude
            🧭 $magnetoX $magnetoY $magnetoZ
            🔀 $gyroX $gyroY $gyroZ
            🏎️ $accelX $accelY $accelZ
            👣 $totalSteps
            📡 ${scannedDevices.toString()}
        """.trimIndent()
        */

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
                totalSteps
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
        magnetoZ: Float? = null,
        steps: Float? = null
    ) {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val writeLatitude = sharedPref.getString("checkBoxLatitude", "true")
        val writeLongitude = sharedPref.getString("checkBoxLongitude", "true")
        val writeHeartRate = sharedPref.getString("checkBoxHeartRate", "true")
        val writeAccelerometer = sharedPref.getString("checkBoxAccelerometer", "true")
        val writeGyroscope = sharedPref.getString("checkBoxGyroscope", "true")
        val writeMagnetometer = sharedPref.getString("checkBoxMagnetometer", "true")
        val writeSteps = sharedPref.getString("checkBoxSteps", "true")

        val data = StringBuilder("$timestamp,")
        if (writeLatitude.toBoolean()) data.append("$latitude,")
        if (writeLongitude.toBoolean()) data.append("$longitude,")
        if (writeHeartRate.toBoolean()) data.append("$heartRate,")
        if (writeAccelerometer.toBoolean()) data.append("$accelX,$accelY,$accelZ,")
        if (writeGyroscope.toBoolean()) data.append("$gyroX,$gyroY,$gyroZ,")
        if (writeMagnetometer.toBoolean()) data.append("$magnetoX,$magnetoY,$magnetoZ,")
        if (writeSteps.toBoolean()) data.append("$steps,")

        // Remove trailing comma and add newline
        if (data.last() == ',') data.setLength(data.length - 1)
        data.append("\n")

        try {
//            csvWriter.append("$timestamp,$latitude,$longitude,$heartRate,$accelX,$accelY,$accelZ,$gyroX,$gyroY,$gyroZ,$magnetoX,$magnetoY,$magnetoZ,$steps\n")
            csvWriter.append(data.toString())
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
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        audioRecorder.handlePermissionsResult(requestCode, grantResults)

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


    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        editText.hint = "Enter Password"

        builder.apply {
            setTitle("Enter Password")
            setView(editText)
            setCancelable(false) // Prevents dialog from being dismissed without interaction

            val dialog = create()
            dialog.show()

            // Request focus and show the keyboard
            editText.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    val enteredPassword = editText.text.toString()
                    val correctPassword = "2211"

                    if (enteredPassword == correctPassword) {
                        val intent = Intent(this@RecordingActivity, SettingsActivity::class.java)
                        startActivity(intent)

                        startButton.visibility = Button.VISIBLE
                        stopButton.visibility = Button.GONE
                        stopRecording()
                        dialog.dismiss()
                        finish()  // Finish the current activity if you don't want to return to it.
                    } else {
                        Toast.makeText(this@RecordingActivity, "Incorrect password", Toast.LENGTH_SHORT).show()
                        // close dialog
                        dialog.dismiss()
                    }
                    true
                } else {
                    false
                }
            }
        }
    }





}

