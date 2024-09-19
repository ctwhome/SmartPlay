package com.example.smartplay

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartplay.utils.FileUtils

/* Global Object Class (singleton) Store to save the application preferences. */
object AppData {
    var userSettings: MutableMap<String, Any> = mutableMapOf()
}

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private val TAG = MainActivity::class.java.simpleName
    }

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.VIBRATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide() // Hide the action bar

        // Create app-specific directory before requesting permissions
        // FileUtils.createAppSpecificDirectoryIfNotExists(this)

        if (!allPermissionsGranted()) {
            requestPermissions()
        } else {
            initializeApp()
        }

        // Notification Button
        val buttonNotify: Button = findViewById(R.id.button_notify)
        buttonNotify.setOnClickListener {
            val intent = Intent(this, NotificationService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_REQUEST_CODE)
        } else {
            initializeApp()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }

            if (deniedPermissions.isEmpty()) {
                Log.i(TAG, "All permissions granted")
                initializeApp()
            } else {
                Log.w(TAG, "Some permissions were not granted: ${deniedPermissions.joinToString(", ")}")
                Toast.makeText(this, "Some features may be limited due to missing permissions", Toast.LENGTH_LONG).show()
                showPermissionExplanation(deniedPermissions)
                initializeApp() // Continue with limited functionality
            }
        }
    }

    private fun showPermissionExplanation(deniedPermissions: List<String>) {
        val explanations = deniedPermissions.mapNotNull { permission ->
            when (permission) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        "Storage access is required to save and read data. Please grant this permission in the app settings."
                    } else {
                        "Storage permissions are required to save and read data."
                    }
                }
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION -> "Location permission is required for accurate tracking."
                Manifest.permission.BODY_SENSORS -> "Body sensors permission is required to monitor your health data."
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT -> "Bluetooth permissions are required to connect to devices."
                Manifest.permission.RECORD_AUDIO -> "Audio recording permission is required for voice commands."
                Manifest.permission.ACTIVITY_RECOGNITION -> "Activity recognition permission is required to count steps."
                else -> null
            }
        }

        val message = explanations.joinToString("\n\n")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun initializeApp() {
        val buttonOpenSecondActivity: Button = findViewById(R.id.button_enter_password)
        buttonOpenSecondActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }

        val buttonRecordingActivity: Button = findViewById(R.id.button_record)
        buttonRecordingActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, RecordingActivity::class.java)
            startActivity(intent)
        }

        val closeAppButton: Button = findViewById(R.id.closeAppButton)
        closeAppButton.setOnClickListener {
            finish()
        }

        showBatteryLevel()
    }

    private fun showBatteryLevel() {
        val batteryLevel = getBatteryLevel()
        findViewById<TextView>(R.id.batteryLevel).text = batteryLevel
    }

    private fun getBatteryLevel(): String {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        val batteryPct = level.toFloat() / scale.toFloat()
        Log.d(TAG, "Battery Level: $batteryPct")
        return "${(batteryPct * 100).toInt()}%"
    }

    override fun onResume() {
        super.onResume()
        showBatteryLevel()
    }

    private fun logOpenGLInfo() {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        Log.d(TAG, "OpenGL ES version: ${configurationInfo.glEsVersion}")
        Log.d(TAG, "Supports OpenGL ES 2.0: ${configurationInfo.reqGlEsVersion >= 0x20000}")
        Log.d(TAG, "Supports OpenGL ES 3.0: ${configurationInfo.reqGlEsVersion >= 0x30000}")
    }
}
