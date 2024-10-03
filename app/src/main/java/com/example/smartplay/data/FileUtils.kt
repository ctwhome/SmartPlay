package com.example.smartplay.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

object FileUtils {
    private const val TAG = "FileUtils"
    private const val FILE_NAME = "workflows.json"
    private const val PREF_NAME = "WorkflowPreferences"
    private const val PREF_KEY_WORKFLOW_FILE = "workflowFile"

    private fun saveToSharedPreferences(context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
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

}
