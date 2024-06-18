package com.example.smartplay

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
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
//import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.smartplay.utils.Workflow
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.hide() // Hide the action bar
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        readFileFromSDCard()
        // Button Record
        val buttonRecordingActivity: Button = findViewById(R.id.button_record)
        buttonRecordingActivity.setOnClickListener {
            val intent = Intent(this@SettingsActivity, RecordingActivity::class.java)
            startActivity(intent)
        }

        //
        // Child ID Input
        //
        val idInput: EditText = findViewById(R.id.id_input)
        idInput.setText(sharedPref.getString("idChild", ""))
        idInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Save the new value to SharedPreferences when the input changes
                saveToSharedPreferences("idChild", s.toString())
            }
        })

        //
        // Frequency button
        //
        // Initialize the EditText
        val frequencyRate: EditText = findViewById(R.id.id_input_frequency)
        // To set a value to the EditText from the SharedPreferences or a default value of 1000
        frequencyRate.setText(sharedPref.getString("frequencyRate", "3000"))
        frequencyRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Save the new value to SharedPreferences when the input changes
                saveToSharedPreferences("frequencyRate", s.toString())
            }
        })

        // Set focus change listeners for the EditTexts, to close keyboard after losing focus
        setupFocusChangeListener(idInput)
        setupFocusChangeListener(frequencyRate)

        //
        // Workflow
        //
        // Initialize the spinner (select)
        val spinnerWorkflow: Spinner = findViewById(R.id.mySpinner)
        // Create an ArrayAdapter using a simple spinner layout and your data
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, getWorkflowList())
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinnerWorkflow.adapter = adapter

        // Set the selected item from the SharedPreferences
        val selectedWorkflow = sharedPref.getString("selectedWorkflow", "")
        if (selectedWorkflow != "") {
            val spinnerPosition = adapter.getPosition(selectedWorkflow)
            spinnerWorkflow.setSelection(spinnerPosition)
        }

        // on spinner change, save the value to shared preferences
        spinnerWorkflow.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                saveToSharedPreferences(
                    "selectedWorkflow", parent.getItemAtPosition(position).toString()
                )
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        //
        // Checkboxes for internal settings
        //

        // checkBoxSound
        val checkBoxSound: CheckBox = findViewById(R.id.checkBoxSound)
        checkBoxSound.isChecked = sharedPref.getString("checkBoxSound", "true").toBoolean()
        checkBoxSound.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxSound", isChecked.toString())
        }

        val checkBoxVibration: CheckBox = findViewById(R.id.checkBoxVibration)
        checkBoxVibration.isChecked = sharedPref.getString("checkBoxVibration", "true").toBoolean()
        checkBoxVibration.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxVibration", isChecked.toString())
        }

        //
        // Checkboxes for the sensors
        //

        // checkBoxAudioRecording
        val checkBoxAudioRecording: CheckBox = findViewById(R.id.checkBoxAudioRecording)
        checkBoxAudioRecording.isChecked = sharedPref.getString("checkBoxAudioRecording", "true").toBoolean()
        checkBoxAudioRecording.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxAudioRecording", isChecked.toString())
        }

        // checkBoxSteps
        val checkBoxSteps: CheckBox = findViewById(R.id.checkBoxSteps)
        checkBoxSteps.isChecked = sharedPref.getString("checkBoxSteps", "true").toBoolean()
        checkBoxSteps.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxSteps", isChecked.toString())
        }

        // checkBoxHeartRate
        val checkBoxHeartRate: CheckBox = findViewById(R.id.checkBoxHeartRate)
        checkBoxHeartRate.isChecked = sharedPref.getString("checkBoxHeartRate", "true").toBoolean()
        checkBoxHeartRate.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxHeartRate", isChecked.toString())
        }

        // checkBoxAccelerometer
        val checkBoxAccelerometer: CheckBox = findViewById(R.id.checkBoxAccelerometer)
        checkBoxAccelerometer.isChecked =
            sharedPref.getString("checkBoxAccelerometer", "true").toBoolean()
        checkBoxAccelerometer.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxAccelerometer", isChecked.toString())
        }

        // checkBoxGyroscope
        val checkBoxGyroscope: CheckBox = findViewById(R.id.checkBoxGyroscope)
        checkBoxGyroscope.isChecked = sharedPref.getString("checkBoxGyroscope", "true").toBoolean()
        checkBoxGyroscope.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxGyroscope", isChecked.toString())
        }

        // checkBoxMagnetometer
        val checkBoxMagnetometer: CheckBox = findViewById(R.id.checkBoxMagnetometer)
        checkBoxMagnetometer.isChecked =
            sharedPref.getString("checkBoxMagnetometer", "true").toBoolean()
        checkBoxMagnetometer.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxMagnetometer", isChecked.toString())
        }
        // checkBoxLocation
        val checkBoxLocation: CheckBox = findViewById(R.id.checkBoxLocation)
        checkBoxLocation.isChecked = sharedPref.getString("checkBoxLocation", "true").toBoolean()
        checkBoxLocation.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxLocation", isChecked.toString())
        }

        // checkBoxBluetoothProximity
        val checkBoxBluetoothProximity: CheckBox = findViewById(R.id.checkBoxBluetoothProximity)
        checkBoxBluetoothProximity.isChecked = sharedPref.getString("checkBoxBluetoothProximity", "true").toBoolean()
        checkBoxBluetoothProximity.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxBluetoothProximity", isChecked.toString())
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

    //
    // Function to get data for the Workflow spinner
    //
    private fun getWorkflowList(): List<String> {
        // read values from the JSON files
        //  return listOf("Choice 1", "Choice 2", "Choice 3", "Choice 4")
        return readFileFromSDCard()
    }


    private fun saveToSharedPreferences(keyName: String, inputValue: String) {
        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(keyName, inputValue)
            apply()
        }
    }

    // Read workflow from JSON file (Original Function)
    private fun readFileFromSDCard(): List<String> {
        val path =
            Environment.getExternalStorageDirectory().path + "/Android/data/com.example.smartplay/files/workflows.json"

        // check if the file exists
        if (!File(path).exists()) {
            Log.d(TAG, "File does not exist")
            return emptyList()
        }
        // log the file content
        saveToSharedPreferences("workflowFile", File(path).readText())

        // return the list of workflow names
        return getWorkflowNamesFromJSON(path)
    }


    private fun getWorkflowNamesFromJSON(path: String): List<String> {
        val jsonContent = File(path).readText()

        val gson = Gson()
        val workflowListType = object : TypeToken<List<Workflow>>() {}.type
        val workflows: List<Workflow> = gson.fromJson(jsonContent, workflowListType)

        return workflows.map { it.workflow_name }
    }
}