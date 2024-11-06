package com.example.smartplay

import android.widget.Button
import android.widget.TextView
import com.example.smartplay.RecordingActivity
import com.example.smartplay.recording.RecordingManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class RecordingActivityTest {

    private lateinit var activity: RecordingActivity
    private lateinit var recordingManager: RecordingManager

    @Before
    fun setup() {
        recordingManager = mock(RecordingManager::class.java)
        activity = Robolectric.buildActivity(RecordingActivity::class.java).create().get()
        activity.recordingManager = recordingManager
    }

    @Test
    fun testRecordingActivityCreation() {
        assertNotNull(activity)
    }

    @Test
    fun testStartRecording() {
        val startButton = activity.findViewById<Button>(com.example.smartplay.R.id.startButton)
        startButton.performClick()

        verify(recordingManager).startRecording()
        assertEquals("Recording...", activity.findViewById<TextView>(com.example.smartplay.R.id.statusText).text)
    }

    @Test
    fun testStopRecording() {
        val stopButton = activity.findViewById<Button>(com.example.smartplay.R.id.stopButton)
        stopButton.performClick()

        verify(recordingManager).stopRecording()
        assertEquals("Recording stopped", activity.findViewById<TextView>(com.example.smartplay.R.id.statusText).text)
    }

    @Test
    fun testPauseRecording() {
        val pauseButton = activity.findViewById<Button>(com.example.smartplay.R.id.pauseButton)
        pauseButton.performClick()

        verify(recordingManager).pauseRecording()
        assertEquals("Recording paused", activity.findViewById<TextView>(com.example.smartplay.R.id.statusText).text)
    }

    @Test
    fun testResumeRecording() {
        val resumeButton = activity.findViewById<Button>(com.example.smartplay.R.id.resumeButton)
        resumeButton.performClick()

        verify(recordingManager).resumeRecording()
        assertEquals("Recording resumed", activity.findViewById<TextView>(com.example.smartplay.R.id.statusText).text)
    }
}
