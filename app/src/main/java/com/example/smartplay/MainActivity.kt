package com.example.smartplay

import android.content.Intent
import android.os.Bundle

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide() // Hide the action bar

        val buttonOpenSecondActivity: Button = findViewById(R.id.button_enter_password)
        buttonOpenSecondActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, PasswordActivity::class.java)
            startActivity(intent)
        }

        val buttonRecordingActivity: Button = findViewById(R.id.button_record)
        buttonRecordingActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, RecordingActivity::class.java)
            startActivity(intent)
        }
    }
}
