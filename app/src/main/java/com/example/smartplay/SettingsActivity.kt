package com.example.smartplay

import android.os.Bundle
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.hide() // Hide the action bar

        val startTimePicker = findViewById<TimePicker>(R.id.start_time)
        startTimePicker.setIs24HourView(true)
        startTimePicker.hour = 10
        startTimePicker.minute= 0

        val stopTimePicker = findViewById<TimePicker>(R.id.stop_time)
        stopTimePicker.setIs24HourView(true)
        stopTimePicker.hour = 11
        stopTimePicker.minute = 30


        val idInput = findViewById<EditText>(R.id.id_input)

        // Use these values in your code
        val startHour = startTimePicker.hour
        val startMinute = startTimePicker.minute

        val stopHour = stopTimePicker.hour
        val stopMinute = stopTimePicker.minute

        val id = idInput.text.toString().toIntOrNull()
    }
}
