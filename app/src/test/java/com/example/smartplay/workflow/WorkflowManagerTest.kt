package com.example.smartplay.workflow

import android.content.Context
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class WorkflowManagerTest {

    private lateinit var context: Context
    private lateinit var workflowManager: WorkflowManager

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        workflowManager = WorkflowManager(context)
    }

    @Test
    fun testInitializeWorkflow_ValidJSON_ReturnsWorkflow() {
        val workflowJson = """
            [
                {
                    "workflow_name": "Test Workflow",
                    "questions": [
                        {
                            "question_id": 1,
                            "question_title": "How are you feeling?",
                            "answers": ["Happy", "Sad", "Neutral"],
                            "time_after_start": 5,
                            "frequency": 3,
                            "time_between_repetitions": 10
                        }
                    ]
                }
            ]
        """.trimIndent()

        val result = workflowManager.initializeWorkflow(workflowJson, "Test Workflow")

        assertNotNull(result)
        assertEquals("Test Workflow", result?.workflow_name)
        assertEquals(1, result?.questions?.size)
    }

    @Test
    fun testInitializeWorkflow_InvalidJSON_ReturnsNull() {
        val invalidJson = "{ invalid json }"

        val result = workflowManager.initializeWorkflow(invalidJson, "Test Workflow")

        assertNull(result)
    }

    @Test
    fun testInitializeWorkflow_NonExistentWorkflowName_ReturnsNull() {
        val workflowJson = """
            [
                {
                    "workflow_name": "Test Workflow",
                    "questions": []
                }
            ]
        """.trimIndent()

        val result = workflowManager.initializeWorkflow(workflowJson, "Non Existent Workflow")

        assertNull(result)
    }

    @Test
    fun testInitializeWorkflow_MultipleWorkflows_SelectsCorrectOne() {
        val workflowJson = """
            [
                {
                    "workflow_name": "Morning Routine",
                    "questions": [
                        {
                            "question_id": 1,
                            "question_title": "Morning question",
                            "answers": ["Yes", "No"],
                            "time_after_start": 5,
                            "frequency": 1,
                            "time_between_repetitions": 60
                        }
                    ]
                },
                {
                    "workflow_name": "Evening Routine",
                    "questions": [
                        {
                            "question_id": 2,
                            "question_title": "Evening question",
                            "answers": ["Good", "Bad"],
                            "time_after_start": 10,
                            "frequency": 1,
                            "time_between_repetitions": 60
                        }
                    ]
                }
            ]
        """.trimIndent()

        val result = workflowManager.initializeWorkflow(workflowJson, "Evening Routine")

        assertNotNull(result)
        assertEquals("Evening Routine", result?.workflow_name)
        assertEquals(2, result?.questions?.get(0)?.question_id)
        assertEquals("Evening question", result?.questions?.get(0)?.question_title)
    }

    @Test
    fun testInitializeWorkflow_WorkflowWithWhitespace_TrimmedCorrectly() {
        val workflowJson = """
            [
                {
                    "workflow_name": " Test Workflow ",
                    "questions": []
                }
            ]
        """.trimIndent()

        val result = workflowManager.initializeWorkflow(workflowJson, "Test Workflow")

        assertNotNull(result)
        assertEquals(" Test Workflow ", result?.workflow_name)
    }

    @Test
    fun testInitializeWorkflow_EmptyQuestions_ParsesSuccessfully() {
        val workflowJson = """
            [
                {
                    "workflow_name": "Empty Workflow",
                    "questions": []
                }
            ]
        """.trimIndent()

        val result = workflowManager.initializeWorkflow(workflowJson, "Empty Workflow")

        assertNotNull(result)
        assertEquals("Empty Workflow", result?.workflow_name)
        assertTrue(result?.questions?.isEmpty() ?: false)
    }

    @Test
    fun testInitializeWorkflow_ComplexQuestion_ParsesAllFields() {
        val workflowJson = """
            [
                {
                    "workflow_name": "Complex Workflow",
                    "questions": [
                        {
                            "question_id": 99,
                            "question_title": "Rate your energy level",
                            "answers": ["Very Low", "Low", "Medium", "High", "Very High"],
                            "time_after_start": 30,
                            "frequency": 5,
                            "time_between_repetitions": 120
                        }
                    ]
                }
            ]
        """.trimIndent()

        val result = workflowManager.initializeWorkflow(workflowJson, "Complex Workflow")

        assertNotNull(result)
        val question = result?.questions?.get(0)
        assertEquals(99, question?.question_id)
        assertEquals("Rate your energy level", question?.question_title)
        assertEquals(5, question?.answers?.size)
        assertEquals("Very Low", question?.answers?.get(0))
        assertEquals(30, question?.time_after_start)
        assertEquals(5, question?.frequency)
        assertEquals(120, question?.time_between_repetitions)
    }

    @Test
    fun testInitializeWorkflow_MultipleQuestions_AllParsed() {
        val workflowJson = """
            [
                {
                    "workflow_name": "Multi Question Workflow",
                    "questions": [
                        {
                            "question_id": 1,
                            "question_title": "Question 1",
                            "answers": ["A", "B"],
                            "time_after_start": 5,
                            "frequency": 1,
                            "time_between_repetitions": 10
                        },
                        {
                            "question_id": 2,
                            "question_title": "Question 2",
                            "answers": ["C", "D"],
                            "time_after_start": 15,
                            "frequency": 2,
                            "time_between_repetitions": 20
                        },
                        {
                            "question_id": 3,
                            "question_title": "Question 3",
                            "answers": ["E", "F"],
                            "time_after_start": 25,
                            "frequency": 3,
                            "time_between_repetitions": 30
                        }
                    ]
                }
            ]
        """.trimIndent()

        val result = workflowManager.initializeWorkflow(workflowJson, "Multi Question Workflow")

        assertNotNull(result)
        assertEquals(3, result?.questions?.size)
        assertEquals("Question 1", result?.questions?.get(0)?.question_title)
        assertEquals("Question 2", result?.questions?.get(1)?.question_title)
        assertEquals("Question 3", result?.questions?.get(2)?.question_title)
    }
}
