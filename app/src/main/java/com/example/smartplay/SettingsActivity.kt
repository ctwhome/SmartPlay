package com.example.smartplay

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.appcompat.app.AppCompatActivity

import com.example.smartplay.utils.Workflow
import com.example.smartplay.utils.FileUtils

class SettingsActivity : AppCompatActivity() {
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
        Log.d("Battery Level", "Battery Level: $batteryPct")
        return "${(batteryPct * 100).toInt()}%"
    }

    override fun onResume() {
        super.onResume()
        showBatteryLevel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.hide() // Hide the action bar

        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        // Button Record
        val buttonRecordingActivity: Button = findViewById(R.id.button_record)
        buttonRecordingActivity.setOnClickListener {
            val intent = Intent(this@SettingsActivity, RecordingActivity::class.java)
            startActivity(intent)
        }

        // Child ID Input
        val idInput: EditText = findViewById(R.id.id_input)
        idInput.setText(sharedPref.getString("idChild", "001"))
        idInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                saveToSharedPreferences("idChild", s.toString())
            }
        })

        // Frequency Input
        val frequencyRate: EditText = findViewById(R.id.id_input_frequency)
        frequencyRate.setText(sharedPref.getString("frequencyRate", "1000"))
        frequencyRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                saveToSharedPreferences("frequencyRate", s.toString())
            }
        })

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

        spinnerWorkflow.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                saveToSharedPreferences("selectedWorkflow", parent.getItemAtPosition(position).toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
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
    }

    private fun setupCheckbox(checkboxId: Int, preferenceName: String) {
        val checkbox: CheckBox = findViewById(checkboxId)
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        checkbox.isChecked = sharedPref.getString(preferenceName, "true").toBoolean()
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
        return FileUtils.readFileFromAppSpecificDirectory(this)
    }

    private fun saveToSharedPreferences(keyName: String, inputValue: String) {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(keyName, inputValue)
            apply()
        }
    }

    private fun getWorkflowNamesFromJSON(path: String): List<String> {
        val jsonContent = java.io.File(path).readText()
        val gson = Gson()
        val workflowListType = object : TypeToken<List<Workflow>>() {}.type
        val workflows: List<Workflow> = gson.fromJson(jsonContent, workflowListType)
        return workflows.map { it.workflow_name }
    }
}