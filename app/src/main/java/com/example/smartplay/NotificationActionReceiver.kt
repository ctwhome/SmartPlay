package com.example.smartplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "com.example.smartplay.GOOD" -> Toast.makeText(context, "Good", Toast.LENGTH_SHORT).show()
            "com.example.smartplay.BAD" -> Toast.makeText(context, "Bad", Toast.LENGTH_SHORT).show()
            "com.example.smartplay.FINE" -> Toast.makeText(context, "Fine", Toast.LENGTH_SHORT).show()
        }
    }
}
