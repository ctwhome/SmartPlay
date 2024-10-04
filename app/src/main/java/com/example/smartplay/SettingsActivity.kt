package com.example.smartplay

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.smartplay.sensors.PermissionManager
import com.example.smartplay.recording.FileUtils

class SettingsActivity : AppCompatActivity() {

    private lateinit var permissionManager: PermissionManager
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.hide() // Hide the action bar

        // Enable and request all permissiona at the start of the application
        permissionManager = PermissionManager(this)
        if (!permissionManager.allPermissionsGranted()) {
            permissionManager.requestPermissions()
        } else {
            initializeSettings()
        }
    }


    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeSettings()
            } else {
                Toast.makeText(
                                this,
                                "Some permissions were not granted. Some features may not work properly.",
                                Toast.LENGTH_LONG
                        )
                        .show()
                initializeSettings() // Still initialize with limited functionality
            }
        }
    }

    private fun initializeSettings() {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        // Button Record
        val buttonRecordingActivity: Button = findViewById(R.id.button_record)
        buttonRecordingActivity.setOnClickListener {
            val intent = Intent(this@SettingsActivity, RecordingActivity::class.java)
            startActivity(intent)
        }

        // Child ID Input
        val idInput: EditText = findViewById(R.id.id_input)
        idInput.setText(sharedPref.getString("idChild", "000"))
        idInput.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        saveToSharedPreferences("idChild", s.toString())
                    }
                }
        )

        // Frequency Input
        val frequencyRate: EditText = findViewById(R.id.id_input_frequency)
        frequencyRate.setText(sharedPref.getString("frequencyRate", "1000"))
        frequencyRate.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        saveToSharedPreferences("frequencyRate", s.toString())
                    }
                }
        )

        // Set focus change listeners for the EditTexts
        setupFocusChangeListener(idInput)
        setupFocusChangeListener(frequencyRate)

        // Workflow Spinner
        val spinnerWorkflow: Spinner = findViewById(R.id.mySpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, getWorkflowList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWorkflow.adapter = adapter

        val selectedWorkflow = sharedPref.getString("selectedWorkflow", "")
        if (selectedWorkflow != "") {
            val spinnerPosition = adapter.getPosition(selectedWorkflow)
            spinnerWorkflow.setSelection(spinnerPosition)
        }

        spinnerWorkflow.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                    ) {
                        parent?.let {
                            val selectedWorkflowName = it.getItemAtPosition(position).toString()
                            saveToSharedPreferences("selectedWorkflow", selectedWorkflowName)

                            // Get the workflow file content (either from cache or by reading the file)
                            val workflowContent = FileUtils.readFileFromAppSpecificDirectory(this@SettingsActivity)
                            if (workflowContent != null) {
                                Log.d("SettingsActivity", "Workflow file content available")
                                // Save the full workflow content to SharedPreferences
                                saveToSharedPreferences("workflowFile", workflowContent)
                            } else {
                                Log.e("SettingsActivity", "Failed to get workflow file content")
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

        // Checkboxes for internal settings
        setupCheckbox(R.id.checkBoxSound, "checkBoxSound")
        setupCheckbox(R.id.checkBoxVibration, "checkBoxVibration")

        // Checkboxes for the sensors
        setupCheckbox(R.id.checkBoxAudioRecording, "checkBoxAudioRecording")
        setupCheckbox(R.id.checkBoxSteps, "checkBoxSteps")
        setupCheckbox(R.id.checkBoxHeartRate, "checkBoxHeartRate")
        setupCheckbox(R.id.checkBoxAccelerometer, "checkBoxAccelerometer")
        setupCheckbox(R.id.checkBoxGyroscope, "checkBoxGyroscope")
        setupCheckbox(R.id.checkBoxMagnetometer, "checkBoxMagnetometer")
        setupCheckbox(R.id.checkBoxLocation, "checkBoxLocation")
        setupCheckbox(R.id.checkBoxBluetoothProximity, "checkBoxBluetoothProximity")
        setupCheckbox(R.id.checkBoxDisplaySensorValues, "checkBoxDisplaySensorValues")
    }

    /* Checkboxes and settings */
    private fun setupCheckbox(checkboxId: Int, preferenceName: String) {
        val checkbox: CheckBox = findViewById(checkboxId)
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        checkbox.isChecked = sharedPref.getString(preferenceName, "false").toBoolean()
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            saveToSharedPreferences(preferenceName, isChecked.toString())
        }
    }
    private fun setupFocusChangeListener(editText: EditText) {
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(view)
            }
        }
    }
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun getWorkflowList(): List<String> {
        /** Force reading the file always from the directory. */
        val workflowFileContent = FileUtils.readFileFromAppSpecificDirectory(this)
        return workflowFileContent?.let { FileUtils.getWorkflowNamesFromContent(it) } ?: emptyList()
    }
    private fun saveToSharedPreferences(keyName: String, inputValue: String) {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(keyName, inputValue)
            apply()
        }
    }

    /* Battery level */
    override fun onResume() {
        super.onResume()
        showBatteryLevel()
    }
    private fun showBatteryLevel() {
        val batteryLevel = getBatteryLevel()
        findViewById<TextView>(R.id.batteryLevel).text = batteryLevel
    }
    private fun getBatteryLevel(): String {
        val batteryStatus: Intent? =
            IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                applicationContext.registerReceiver(null, ifilter)
            }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        val batteryPct = level.toFloat() / scale.toFloat()
        Log.d("Battery Level", "Battery Level: $batteryPct")
        return "${(batteryPct * 100).toInt()}%"
    }
}