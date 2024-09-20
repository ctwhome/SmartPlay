
package com.example.smartplay

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private val TAG = PermissionManager::class.java.simpleName
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

    fun allPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest, PERMISSION_REQUEST_CODE)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }

            if (deniedPermissions.isEmpty()) {
                Log.i(TAG, "All permissions granted")
                return true
            } else {
                Log.w(TAG, "Some permissions were not granted: ${deniedPermissions.joinToString(", ")}")
                Toast.makeText(activity, "Some features may be limited due to missing permissions", Toast.LENGTH_LONG).show()
                showPermissionExplanation(deniedPermissions)
                return false
            }
        }
        return false
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
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }
}