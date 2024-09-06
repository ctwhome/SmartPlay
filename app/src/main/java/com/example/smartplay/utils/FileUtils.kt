package com.yourpackage.utils // Replace with your actual package name

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartplay.SettingsActivity
import java.io.File
import java.io.IOException

object FileUtils {
    private const val TAG = "FileUtils"
    private const val PERMISSION_REQUEST_CODE = 101

    fun requestPermissions(activity: Activity) {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), PERMISSION_REQUEST_CODE)
        } else {
            Log.d(TAG, "Permission already granted")
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray, onPermissionGranted: SettingsActivity) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, call the callback function
//            onPermissionGranted()
        } else {
            Log.d(TAG, "Permission denied")
        }
    }

    fun readFileFromSDCard(context: Context): List<String> {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission not granted")
            return emptyList()
        }

        val path = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/workflows.json"

        if (!File(path).exists()) {
            Log.d(TAG, "File does not exist")
            return emptyList()
        }

        val fileContent = try {
            File(path).readText()
        } catch (e: IOException) {
            Log.e(TAG, "Error reading file", e)
            return emptyList()
        }

        Log.d(TAG, "File content: $fileContent")

        return getWorkflowNamesFromJSON(fileContent)
    }

    private fun getWorkflowNamesFromJSON(fileContent: String): List<String> {
        // Implement your JSON parsing here and return the list of workflow names
        // For now, returning an empty list for demonstration purposes
        return emptyList()
    }
}
