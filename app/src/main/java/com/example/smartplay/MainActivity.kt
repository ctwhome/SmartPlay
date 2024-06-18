package com.example.smartplay

import java.io.File
import android.app.AlertDialog
import android.content.Context

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log

import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


/* Global Object Class (singleton) Store to save the application preferences. */
object AppData {
    var userSettings: MutableMap<String, Any> = mutableMapOf()
}

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create an Intent to start SettingsActivity
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)

        // Finish MainActivity so it's not on the back stack
        finish()

        return
        supportActionBar?.hide() // Hide the action bar

        val buttonOpenSecondActivity: Button = findViewById(R.id.button_enter_password)
        buttonOpenSecondActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            // Check it the /storage/emulated/0/Android/data/com.example.smartplay/files/workflows.json file exists
            val file = applicationContext.getFileStreamPath("workflows.json")
            if (file.exists()) {
                startActivity(intent)
            }
//            else {
//                // Show a pop-up message to the user to inform there is no workflow file set up.
//                showMessageDialog(this)

//                // Create the workflow file
//                createWorkflowJsonFile(this)
//                // go to the settings activity
//                startActivity(intent)
//
//            }
        }

        val buttonRecordingActivity: Button = findViewById(R.id.button_record)
        buttonRecordingActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, RecordingActivity::class.java)
            startActivity(intent)
        }

        // Find the close app button by its ID
        val closeAppButton: Button = findViewById(R.id.closeAppButton)
        // Set a click listener for the button
        closeAppButton.setOnClickListener {
            // Close the app
            finish()
        }
    }


    //    show battery level in the @+id/batteryLevel field
    fun showBatteryLevel() {
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
        // log battery level
        Log.d("Battery Level", "Battery Level: $batteryPct")
        return "${(batteryPct * 100).toInt()}%"

    }

    override fun onResume() {
        super.onResume()
        showBatteryLevel()
    }

}
