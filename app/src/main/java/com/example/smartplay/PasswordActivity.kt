package com.example.smartplay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class PasswordActivity : AppCompatActivity() {

    private val correctPassword = "1234"
    private var enteredPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        supportActionBar?.hide() // Hide the action bar

        // Set up click listeners for all the buttons
        val buttons = listOf(
            findViewById<Button>(R.id.button0),
            findViewById<Button>(R.id.button1),
            findViewById<Button>(R.id.button2),
            findViewById<Button>(R.id.button3),
            findViewById<Button>(R.id.button4),
            findViewById<Button>(R.id.button5),
            findViewById<Button>(R.id.button6),
            findViewById<Button>(R.id.button7),
            findViewById<Button>(R.id.button8),
            findViewById<Button>(R.id.button9)
        )

        for (button in buttons) {
            button.setOnClickListener {
                // Append the number to the entered password
                enteredPassword += button.text

                // Update the TextView to reflect the entered password
                findViewById<TextView>(R.id.password_text).text = enteredPassword

                // If the entered password is correct, navigate to the main activity
                if (enteredPassword == correctPassword) {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        // Set up the clear button
        findViewById<Button>(R.id.button_clear).setOnClickListener {
            enteredPassword = ""
            findViewById<TextView>(R.id.password_text).text = enteredPassword
        }
    }
}
