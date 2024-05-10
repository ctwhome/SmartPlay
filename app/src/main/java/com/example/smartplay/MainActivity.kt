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

class MainActivity : AppCompatActivity() {


    fun createWorkflowJsonFile(context: Context) {
        val content = """
    [
      {
        "workflow_name":"Workflow 1",
        "questions":[
          {
            "question_id":1,
            "question_title":"How do you feel?",
            "answers":[
              "👍",
              "🤢",
              "🤷‍♀️"
            ],
            "time_after_start_in_minutes":"10",
            "frequency":1
          },
          {
            "question_id":2,
            "question_title":"Energy level?",
            "answers":[
              "🔋",
              "🪫"
            ],
            "time_after_start_in_minutes":2,
            "frequency_in_minutes":5
          },
          {
            "question_id":3,
            "question_title":"Feeling alone?",
            "answers":[
              "😭",
              "🥳"
            ],
            "time_after_start_in_minutes":5,
            "frequency_in_minutes":20
          }
        ]
      },
      {
        "workflow_name":"Workflow 2",
        "questions":[

        ]
      },
      {
        "workflow_name":"Workflow 3",
        "questions":[

        ]
      }
    ]
    """.trimIndent()

        // Use getExternalFilesDir for accessing app's external files directory; no permission required for this directory on API 19 and above
        // Correct way to handle file creation without using getFileStreamPath
        val fileDir = context.getExternalFilesDir(null)?.absolutePath + "/workflow"
        val workflowJsonFile = File(fileDir, "workflow.json")

        if (!workflowJsonFile.exists()) {
            workflowJsonFile.parentFile?.mkdirs() // Ensure the directory exists
            workflowJsonFile.writeText(content)
        }
    }

    fun showMessageDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Message Title")
        builder.setMessage("No Workflow File")
        builder.setPositiveButton("OK") { dialog, which ->
            // Handle the OK button click here
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            // Handle the Cancel button click here
        }

        // Create and show the AlertDialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide() // Hide the action bar

        val buttonOpenSecondActivity: Button = findViewById(R.id.button_enter_password)
        buttonOpenSecondActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            // Check it the /storage/emulated/0/Android/data/com.example.smartplay/files/workflow/workflow.json file exists
            val file = applicationContext.getFileStreamPath("workflow/workflow.json")
            if (file.exists()) {
                startActivity(intent)
            }
//            else {
//                // Show a pop-up message to the user to inform there is no workflow file set up.
//                showMessageDialog(this)
//
//
//
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
        return "${batteryPct * 100}%"

    }

    override fun onResume() {
        super.onResume()
        showBatteryLevel()
    }
}
