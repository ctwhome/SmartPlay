package com.example.smartplay

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasswordActivity : AppCompatActivity() {

    private val correctPassword = "2122"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        supportActionBar?.hide() // Hide the action bar

        val passwordInput = findViewById<EditText>(R.id.password_input)
        val submitButton = findViewById<Button>(R.id.submit_button)

        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkPassword(passwordInput.text.toString())
                true
            } else {
                false
            }
        }

        submitButton.setOnClickListener {
            checkPassword(passwordInput.text.toString())
        }

        // Set focus to the password input and show the keyboard
        passwordInput.requestFocus()
        Handler(Looper.getMainLooper()).postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(passwordInput, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun checkPassword(enteredPassword: String) {
        if (enteredPassword == correctPassword) {
            setResult(RESULT_OK)
        } else {
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
            findViewById<EditText>(R.id.password_input).text.clear()
        }
        finish()
    }

}
