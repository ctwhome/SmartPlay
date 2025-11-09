package com.example.smartplay.workflow

import org.junit.Test
import org.junit.Assert.*
import com.google.gson.Gson

class WorkflowTypesTest {

    @Test
    fun testQuestionCreation() {
        val question = Question(
            question_id = 1,
            question_title = "How are you feeling?",
            answers = listOf("Happy", "Sad", "Neutral"),
            time_after_start = 5,
            frequency = 3,
            time_between_repetitions = 10
        )

        assertEquals(1, question.question_id)
        assertEquals("How are you feeling?", question.question_title)
        assertEquals(3, question.answers.size)
        assertEquals("Happy", question.answers[0])
        assertEquals(5, question.time_after_start)
        assertEquals(3, question.frequency)
        assertEquals(10, question.time_between_repetitions)
    }

    @Test
    fun testWorkflowCreation() {
        val questions = listOf(
            Question(1, "Question 1", listOf("A", "B"), 5, 1, 10),
            Question(2, "Question 2", listOf("C", "D"), 15, 2, 20)
        )

        val workflow = Workflow(
            workflow_name = "Test Workflow",
            questions = questions
        )

        assertEquals("Test Workflow", workflow.workflow_name)
        assertEquals(2, workflow.questions.size)
        assertEquals("Question 1", workflow.questions[0].question_title)
        assertEquals("Question 2", workflow.questions[1].question_title)
    }

    @Test
    fun testWorkflowSerializationAndDeserialization() {
        val originalQuestion = Question(
            question_id = 1,
            question_title = "Test Question",
            answers = listOf("Answer 1", "Answer 2", "Answer 3"),
            time_after_start = 10,
            frequency = 2,
            time_between_repetitions = 30
        )

        val originalWorkflow = Workflow(
            workflow_name = "Serialization Test",
            questions = listOf(originalQuestion)
        )

        val gson = Gson()
        val json = gson.toJson(originalWorkflow)
        val deserializedWorkflow = gson.fromJson(json, Workflow::class.java)

        assertEquals(originalWorkflow.workflow_name, deserializedWorkflow.workflow_name)
        assertEquals(originalWorkflow.questions.size, deserializedWorkflow.questions.size)
        assertEquals(
            originalWorkflow.questions[0].question_title,
            deserializedWorkflow.questions[0].question_title
        )
        assertEquals(
            originalWorkflow.questions[0].answers.size,
            deserializedWorkflow.questions[0].answers.size
        )
    }

    @Test
    fun testQuestionEquality() {
        val question1 = Question(
            question_id = 1,
            question_title = "Question",
            answers = listOf("A", "B"),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        val question2 = Question(
            question_id = 1,
            question_title = "Question",
            answers = listOf("A", "B"),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        assertEquals(question1, question2)
        assertEquals(question1.hashCode(), question2.hashCode())
    }

    @Test
    fun testWorkflowEquality() {
        val questions = listOf(
            Question(1, "Q1", listOf("A"), 5, 1, 10)
        )

        val workflow1 = Workflow("Test", questions)
        val workflow2 = Workflow("Test", questions)

        assertEquals(workflow1, workflow2)
        assertEquals(workflow1.hashCode(), workflow2.hashCode())
    }

    @Test
    fun testQuestionCopy() {
        val original = Question(
            question_id = 1,
            question_title = "Original",
            answers = listOf("A", "B"),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        val copy = original.copy(question_title = "Modified")

        assertEquals(1, copy.question_id)
        assertEquals("Modified", copy.question_title)
        assertEquals(original.answers, copy.answers)
        assertNotEquals(original, copy)
    }

    @Test
    fun testWorkflowCopy() {
        val questions = listOf(Question(1, "Q1", listOf("A"), 5, 1, 10))
        val original = Workflow("Original", questions)
        val copy = original.copy(workflow_name = "Modified")

        assertEquals("Modified", copy.workflow_name)
        assertEquals(original.questions, copy.questions)
        assertNotEquals(original, copy)
    }

    @Test
    fun testEmptyAnswersList() {
        val question = Question(
            question_id = 1,
            question_title = "No answers",
            answers = emptyList(),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        assertTrue(question.answers.isEmpty())
    }

    @Test
    fun testEmptyQuestionsList() {
        val workflow = Workflow(
            workflow_name = "Empty Workflow",
            questions = emptyList()
        )

        assertTrue(workflow.questions.isEmpty())
    }

    @Test
    fun testQuestionWithManyAnswers() {
        val answers = (1..10).map { "Answer $it" }
        val question = Question(
            question_id = 1,
            question_title = "Many answers",
            answers = answers,
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        assertEquals(10, question.answers.size)
        assertEquals("Answer 1", question.answers[0])
        assertEquals("Answer 10", question.answers[9])
    }

    @Test
    fun testWorkflowWithManyQuestions() {
        val questions = (1..20).map {
            Question(
                question_id = it,
                question_title = "Question $it",
                answers = listOf("A", "B"),
                time_after_start = it * 5,
                frequency = 1,
                time_between_repetitions = 10
            )
        }

        val workflow = Workflow("Large Workflow", questions)

        assertEquals(20, workflow.questions.size)
        assertEquals("Question 1", workflow.questions[0].question_title)
        assertEquals("Question 20", workflow.questions[19].question_title)
    }

    @Test
    fun testQuestionFieldBoundaryValues() {
        val question = Question(
            question_id = Int.MAX_VALUE,
            question_title = "Max values",
            answers = listOf("A"),
            time_after_start = Int.MAX_VALUE,
            frequency = Int.MAX_VALUE,
            time_between_repetitions = Int.MAX_VALUE
        )

        assertEquals(Int.MAX_VALUE, question.question_id)
        assertEquals(Int.MAX_VALUE, question.time_after_start)
        assertEquals(Int.MAX_VALUE, question.frequency)
        assertEquals(Int.MAX_VALUE, question.time_between_repetitions)
    }
}
