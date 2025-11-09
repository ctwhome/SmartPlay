package com.example.smartplay.recording

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.junit.Assert.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
class DataRecorderTest {

    private lateinit var context: Context
    private lateinit var dataRecorder: DataRecorder
    private lateinit var testDir: File
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        dataRecorder = DataRecorder(context)
        testDir = context.getExternalFilesDir(null)!!

        // Setup SharedPreferences with default values
        sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("checkBoxLatitude", "true")
            putString("checkBoxLongitude", "true")
            putString("checkBoxHeartRate", "true")
            putString("checkBoxAccelerometer", "true")
            putString("checkBoxGyroscope", "true")
            putString("checkBoxMagnetometer", "true")
            putString("checkBoxSteps", "true")
            apply()
        }

        // Clean up any existing test files
        testDir.listFiles()?.filter { it.name.contains("TEST") }?.forEach { it.delete() }
    }

    @Test
    fun testInitializeFiles_CreatesAllFiles() {
        val childId = "TEST001"
        val watchId = "WATCH001"
        val timestamp = System.currentTimeMillis()

        dataRecorder.initializeFiles(childId, watchId, timestamp)

        val sensorFile = File(testDir, "${childId}_SENSORS_${watchId}_$timestamp.csv")
        val btFile = File(testDir, "${childId}_BT_${watchId}_$timestamp.csv")
        val questionFile = File(testDir, "${childId}_QUESTIONS_${watchId}_$timestamp.csv")

        assertTrue(sensorFile.exists())
        assertTrue(btFile.exists())
        assertTrue(questionFile.exists())

        // Clean up
        sensorFile.delete()
        btFile.delete()
        questionFile.delete()
    }

    @Test
    fun testWriteSensorData_WritesCorrectFormat() {
        val childId = "TEST002"
        val watchId = "WATCH002"
        val timestamp = System.currentTimeMillis()

        dataRecorder.initializeFiles(childId, watchId, timestamp)

        val sensorData = mapOf(
            "timestamp" to timestamp,
            "latitude" to 52.2296756,
            "longitude" to 6.9940711,
            "heartRate" to 75,
            "accelX" to 0.1,
            "accelY" to 0.2,
            "accelZ" to 9.8,
            "gyroX" to 0.01,
            "gyroY" to 0.02,
            "gyroZ" to 0.03,
            "magnetoX" to 25.0,
            "magnetoY" to 30.0,
            "magnetoZ" to 35.0,
            "steps" to 1234
        )

        dataRecorder.writeSensorData(sensorData)
        dataRecorder.closeFiles()

        val sensorFile = File(testDir, "${childId}_SENSORS_${watchId}_$timestamp.csv")
        val content = sensorFile.readText()

        assertTrue(content.contains("timestamp"))
        assertTrue(content.contains("latitude"))
        assertTrue(content.contains(timestamp.toString()))
        assertTrue(content.contains("75"))

        // Clean up
        sensorFile.delete()
        File(testDir, "${childId}_BT_${watchId}_$timestamp.csv").delete()
        File(testDir, "${childId}_QUESTIONS_${watchId}_$timestamp.csv").delete()
    }

    @Test
    fun testWriteBluetoothData_WritesCorrectFormat() {
        val childId = "TEST003"
        val watchId = "WATCH003"
        val timestamp = System.currentTimeMillis()

        dataRecorder.initializeFiles(childId, watchId, timestamp)

        val devices = mapOf(
            "Device1" to -50,
            "Device2" to -70,
            "Device3" to -80
        )

        dataRecorder.writeBluetoothData(timestamp, devices)
        dataRecorder.closeFiles()

        val btFile = File(testDir, "${childId}_BT_${watchId}_$timestamp.csv")
        val content = btFile.readText()

        assertTrue(content.contains("timestamp,devices"))
        assertTrue(content.contains("Device1--50"))
        assertTrue(content.contains("Device2--70"))

        // Clean up
        btFile.delete()
        File(testDir, "${childId}_SENSORS_${watchId}_$timestamp.csv").delete()
        File(testDir, "${childId}_QUESTIONS_${watchId}_$timestamp.csv").delete()
    }

    @Test
    fun testWriteQuestionData_WritesCorrectFormat() {
        val childId = "TEST004"
        val watchId = "WATCH004"
        val timestamp = System.currentTimeMillis()

        dataRecorder.initializeFiles(childId, watchId, timestamp)

        val questionTimestamp = System.currentTimeMillis()
        val questionID = "Q001"
        val questionText = "How do you feel?"
        val answer = "Happy"

        dataRecorder.writeQuestionData(questionTimestamp, questionID, questionText, answer)
        dataRecorder.closeFiles()

        val questionFile = File(testDir, "${childId}_QUESTIONS_${watchId}_$timestamp.csv")
        val content = questionFile.readText()

        assertTrue(content.contains("timestamp,questionID,questionText,answer"))
        assertTrue(content.contains(questionID))
        assertTrue(content.contains(questionText))
        assertTrue(content.contains(answer))

        // Clean up
        questionFile.delete()
        File(testDir, "${childId}_SENSORS_${watchId}_$timestamp.csv").delete()
        File(testDir, "${childId}_BT_${watchId}_$timestamp.csv").delete()
    }

    @Test
    fun testWriteSensorData_BeforeInitialization_DoesNotCrash() {
        val uninitializedRecorder = DataRecorder(context)

        val sensorData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "latitude" to 52.2296756
        )

        // Should not throw exception, just log error
        uninitializedRecorder.writeSensorData(sensorData)

        // Test passes if no exception is thrown
        assertTrue(true)
    }

    @Test
    fun testWriteBluetoothData_BeforeInitialization_DoesNotCrash() {
        val uninitializedRecorder = DataRecorder(context)

        val devices = mapOf("Device1" to -50)

        // Should not throw exception, just log error
        uninitializedRecorder.writeBluetoothData(System.currentTimeMillis(), devices)

        // Test passes if no exception is thrown
        assertTrue(true)
    }

    @Test
    fun testWriteQuestionData_BeforeInitialization_DoesNotCrash() {
        val uninitializedRecorder = DataRecorder(context)

        // Should not throw exception, just log error
        uninitializedRecorder.writeQuestionData(
            System.currentTimeMillis(),
            "Q001",
            "Test question",
            "Test answer"
        )

        // Test passes if no exception is thrown
        assertTrue(true)
    }

    @Test
    fun testCloseFiles_DoesNotCrash() {
        val childId = "TEST005"
        val watchId = "WATCH005"
        val timestamp = System.currentTimeMillis()

        dataRecorder.initializeFiles(childId, watchId, timestamp)
        dataRecorder.closeFiles()

        // Test passes if no exception is thrown
        assertTrue(true)

        // Clean up
        File(testDir, "${childId}_SENSORS_${watchId}_$timestamp.csv").delete()
        File(testDir, "${childId}_BT_${watchId}_$timestamp.csv").delete()
        File(testDir, "${childId}_QUESTIONS_${watchId}_$timestamp.csv").delete()
    }

    @Test
    fun testSensorHeader_RespectsPreferences() {
        val childId = "TEST006"
        val watchId = "WATCH006"
        val timestamp = System.currentTimeMillis()

        // Disable some sensors
        with(sharedPreferences.edit()) {
            putString("checkBoxHeartRate", "false")
            putString("checkBoxGyroscope", "false")
            apply()
        }

        dataRecorder.initializeFiles(childId, watchId, timestamp)
        dataRecorder.closeFiles()

        val sensorFile = File(testDir, "${childId}_SENSORS_${watchId}_$timestamp.csv")
        val header = sensorFile.readLines().firstOrNull() ?: ""

        assertFalse(header.contains("heartRate"))
        assertFalse(header.contains("gyro"))
        assertTrue(header.contains("latitude"))
        assertTrue(header.contains("longitude"))

        // Clean up
        sensorFile.delete()
        File(testDir, "${childId}_BT_${watchId}_$timestamp.csv").delete()
        File(testDir, "${childId}_QUESTIONS_${watchId}_$timestamp.csv").delete()
    }
}
