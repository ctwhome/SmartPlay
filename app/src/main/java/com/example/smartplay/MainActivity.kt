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

        val buttonOpenSecondActivity: Button = findViewById(R.id.button_open_second_activity)
        buttonOpenSecondActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, PasswordActivity::class.java)
            startActivity(intent)
        }
    }
}
