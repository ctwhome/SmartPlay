package com.example.smartplay.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartplay.SettingsActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

object FileUtils {
    private const val TAG = "FileUtils"
    private const val PERMISSION_REQUEST_CODE = 101
    private const val FILE_NAME = "workflows.json"
    private const val PREF_NAME = "WorkflowPreferences"
    private const val PREF_KEY_WORKFLOW_FILE = "workflowFile"

    fun requestPermissions(activity: Activity) {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(activity, permission) !=
                        PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permission),
                    PERMISSION_REQUEST_CODE
            )
        } else {
            Log.d(TAG, "Permission already granted")
        }
    }

    fun onRequestPermissionsResult(
            requestCode: Int,
            grantResults: IntArray,
            onPermissionGranted: SettingsActivity
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE &&
                        grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, call the callback function
            // onPermissionGranted.handlePermissionGranted() // Uncomment and adjust according to
            // your implementation
        } else {
            Log.d(TAG, "Permission denied")
        }
    }

    fun readFileFromAppSpecificDirectory(context: Context): String? {
        val appSpecificInternalFile = File(getAppSpecificInternalDirectory(context), FILE_NAME)
        val appSpecificExternalFile = File(context.getExternalFilesDir(null), FILE_NAME)

        val fileContent =
                when {
                    appSpecificInternalFile.exists() -> {
                        Log.d(TAG, "File found in app-specific internal directory")
                        readFile(appSpecificInternalFile)
                    }
                    appSpecificExternalFile.exists() -> {
                        Log.d(TAG, "File found in app-specific external directory")
                        readFile(appSpecificExternalFile)
                    }
                    else -> {
                        Log.d(TAG, "File not found in either location")
                        null
                    }
                }

        fileContent?.let {
            Log.d(TAG, "File content read successfully")
            saveToSharedPreferences(context, PREF_KEY_WORKFLOW_FILE, it)
        }

        return fileContent
    }

    private fun readFile(file: File): String? {
        return try {
            file.readText()
        } catch (e: IOException) {
            Log.e(TAG, "Error reading file", e)
            null
        }
    }

    private fun getAppSpecificInternalDirectory(context: Context): File {
        return context.filesDir
    }

    fun getAppSpecificDirectory(context: Context): File {
        val directory = context.getExternalFilesDir(null) ?: context.filesDir
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    fun getWorkflowNamesFromContent(fileContent: String): List<String> {
        return try {
            val gson = Gson()
            val workflowListType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val workflows: List<Map<String, Any>> = gson.fromJson(fileContent, workflowListType)
            workflows.mapNotNull { it["workflow_name"] as? String }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON", e)
            emptyList()
        }
    }

    fun createAppSpecificDirectoryIfNotExists(context: Context) {
        val directory =
                File(
                        Environment.getExternalStorageDirectory(),
                        "Android/data/${context.packageName}/files/"
                )
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                Log.d(TAG, "App-specific directory created successfully: ${directory.absolutePath}")
            } else {
                Log.e(TAG, "Failed to create app-specific directory: ${directory.absolutePath}")
            }
        } else {
            Log.d(TAG, "App-specific directory already exists: ${directory.absolutePath}")
        }
    }

    fun readWorkflowFileContent(context: Context, workflowName: String): String? {
        val fileContent =
                getWorkflowFileFromSharedPreferences(context)
                        ?: readFileFromAppSpecificDirectory(context)

        if (fileContent == null) {
            Log.e(TAG, "Workflow file content not found")
            return null
        }

        return try {
            val gson = Gson()
            val workflowListType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val workflows: List<Map<String, Any>> = gson.fromJson(fileContent, workflowListType)
            val selectedWorkflow = workflows.find { it["workflow_name"] == workflowName }
            if (selectedWorkflow != null) {
                gson.toJson(selectedWorkflow)
            } else {
                Log.e(TAG, "Selected workflow not found: $workflowName")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON for workflow: $workflowName", e)
            null
        }
    }

    private fun saveToSharedPreferences(context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun getWorkflowFileFromSharedPreferences(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PREF_KEY_WORKFLOW_FILE, null)
    }
}
