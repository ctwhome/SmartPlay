package com.example.smartplay.tests

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.smartplay.SettingsActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class SettingsActivityTest {

    private lateinit var activity: SettingsActivity
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(SettingsActivity::class.java).create().get()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

    @Test
    fun testSettingsActivityCreation() {
        assertNotNull(activity)
    }

    @Test
    fun testDefaultSettings() {
        // Test default values for key settings
        assertTrue(sharedPreferences.getBoolean("enable_notifications", true))
        assertEquals("Default Workflow", sharedPreferences.getString("default_workflow", ""))
        assertFalse(sharedPreferences.getBoolean("dark_mode", false))
    }

    @Test
    fun testChangeSettings() {
        // Simulate changing settings
        with(sharedPreferences.edit()) {
            putBoolean("enable_notifications", false)
            putString("default_workflow", "Custom Workflow")
            putBoolean("dark_mode", true)
            apply()
        }

        // Verify changes
        assertFalse(sharedPreferences.getBoolean("enable_notifications", true))
        assertEquals("Custom Workflow", sharedPreferences.getString("default_workflow", ""))
        assertTrue(sharedPreferences.getBoolean("dark_mode", false))
    }
}