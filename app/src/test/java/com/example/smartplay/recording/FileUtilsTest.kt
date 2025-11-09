package com.example.smartplay.recording

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.junit.Assert.*
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class FileUtilsTest {

    private lateinit var context: Context
    private lateinit var testFilesDir: File

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()
        testFilesDir = context.filesDir

        // Clean up any existing test files
        File(testFilesDir, "workflows.json").delete()
    }

    @Test
    fun testReadFileFromAppSpecificDirectory_FileExistsInInternal() {
        // Create a test workflow file in internal directory
        val testContent = """
            [
                {
                    "workflow_name": "Test Workflow",
                    "questions": []
                }
            ]
        """.trimIndent()

        val testFile = File(testFilesDir, "workflows.json")
        testFile.writeText(testContent)

        val result = FileUtils.readFileFromAppSpecificDirectory(context)

        assertNotNull(result)
        assertTrue(result!!.contains("Test Workflow"))
    }

    @Test
    fun testReadFileFromAppSpecificDirectory_FileNotExists() {
        val result = FileUtils.readFileFromAppSpecificDirectory(context)
        assertNull(result)
    }

    @Test
    fun testGetWorkflowNamesFromContent_ValidJSON() {
        val jsonContent = """
            [
                {
                    "workflow_name": "Morning Routine",
                    "questions": []
                },
                {
                    "workflow_name": "Evening Routine",
                    "questions": []
                }
            ]
        """.trimIndent()

        val workflowNames = FileUtils.getWorkflowNamesFromContent(jsonContent)

        assertEquals(2, workflowNames.size)
        assertTrue(workflowNames.contains("Morning Routine"))
        assertTrue(workflowNames.contains("Evening Routine"))
    }

    @Test
    fun testGetWorkflowNamesFromContent_InvalidJSON() {
        val invalidJson = "{ invalid json }"

        val workflowNames = FileUtils.getWorkflowNamesFromContent(invalidJson)

        assertTrue(workflowNames.isEmpty())
    }

    @Test
    fun testGetWorkflowNamesFromContent_EmptyArray() {
        val emptyJson = "[]"

        val workflowNames = FileUtils.getWorkflowNamesFromContent(emptyJson)

        assertTrue(workflowNames.isEmpty())
    }

    @Test
    fun testGetWorkflowNamesFromContent_MissingWorkflowNameField() {
        val jsonWithoutNames = """
            [
                {
                    "questions": []
                }
            ]
        """.trimIndent()

        val workflowNames = FileUtils.getWorkflowNamesFromContent(jsonWithoutNames)

        assertTrue(workflowNames.isEmpty())
    }

    @Test
    fun testGetWorkflowNamesFromContent_MixedValidAndInvalidEntries() {
        val mixedJson = """
            [
                {
                    "workflow_name": "Valid Workflow",
                    "questions": []
                },
                {
                    "questions": []
                },
                {
                    "workflow_name": "Another Valid",
                    "questions": []
                }
            ]
        """.trimIndent()

        val workflowNames = FileUtils.getWorkflowNamesFromContent(mixedJson)

        assertEquals(2, workflowNames.size)
        assertTrue(workflowNames.contains("Valid Workflow"))
        assertTrue(workflowNames.contains("Another Valid"))
    }
}
