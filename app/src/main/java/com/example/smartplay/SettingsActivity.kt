package com.example.smartplay

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Spinner
//import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


data class Workflow(
    val workflow_name: String,
    val questions: List<Question>
)

data class Question(
    val question_id: Int,
    val question_title: String,
    val answers: List<String>,
    val time_after_start_in_minutes: Any,  // This can be String or Int based on the example
    val frequency: Int? = null,
    val frequency_in_minutes: Int? = null
)
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
        idInput.setText(sharedPref.getString("idChild", "000"))
        idInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(s: Editable?) {
                // Save the new value to SharedPreferences when the input changes
                saveToSharedPreferences("idChild",s.toString())
            }
        })


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


        //
        // Checkboxes for the sensors
        //
        val checkBoxHeartRate: CheckBox = findViewById(R.id.checkBoxHeartRate)
        checkBoxHeartRate.isChecked = sharedPref.getString("checkBoxHeartRate", "true").toBoolean()
        checkBoxHeartRate.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxHeartRate", isChecked.toString())
        }
        val checkBoxAccelerometer: CheckBox = findViewById(R.id.checkBoxAccelerometer)
        checkBoxAccelerometer.isChecked = sharedPref.getString("checkBoxAccelerometer", "true").toBoolean()
        checkBoxAccelerometer.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxAccelerometer", isChecked.toString())
        }
        val checkBoxGyroscope: CheckBox = findViewById(R.id.checkBoxGyroscope)
        checkBoxGyroscope.isChecked = sharedPref.getString("checkBoxGyroscope", "true").toBoolean()
        checkBoxGyroscope.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxGyroscope", isChecked.toString())
        }
        val checkBoxMagnetometer: CheckBox = findViewById(R.id.checkBoxMagnetometer)
        checkBoxMagnetometer.isChecked = sharedPref.getString("checkBoxMagnetometer", "true").toBoolean()
        checkBoxMagnetometer.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxMagnetometer", isChecked.toString())
        }
        val checkBoxLocation: CheckBox = findViewById(R.id.checkBoxLocation)
        checkBoxLocation.isChecked = sharedPref.getString("checkBoxLocation", "true").toBoolean()
        checkBoxLocation.setOnCheckedChangeListener { buttonView, isChecked -> // Handle the checkbox state change here
            saveToSharedPreferences("checkBoxLocation", isChecked.toString())
        }

        //
        // Frequency button
        //
        // Initialize the EditText
        val frequencyRate: EditText = findViewById(R.id.id_input_frequency)
        // To set a value to the EditText from the SharedPreferences or a default value of 1000
        frequencyRate.setText(sharedPref.getString("frequencyRate", "3000"))
        frequencyRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(s: Editable?) {
                // Save the new value to SharedPreferences when the input changes
                saveToSharedPreferences("frequencyRate",s.toString())
            }
        })



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

    // read workflow from JSON file

    private fun readFileFromSDCard(): List<String> {
        val path = Environment.getExternalStorageDirectory().path + "/Android/data/com.example.smartplay/files/workflow/workflow.json"
        Log.d(TAG, "Path: $path")
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

