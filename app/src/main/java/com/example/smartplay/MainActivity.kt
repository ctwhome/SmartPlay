package com.example.smartplay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/* Global Object Class (singleton) Store to save the application preferences. */
object AppData {
    var userSettings: MutableMap<String, Any> = mutableMapOf()
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immediately start SettingsActivity
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        finish() // Close MainActivity so it's not in the back stack
    }
}
