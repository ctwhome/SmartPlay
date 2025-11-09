package com.example.smartplay.workflow.notifications

import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import com.example.smartplay.workflow.Question
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class NotificationManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var systemNotificationManager: AndroidNotificationManager

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        notificationManager = NotificationManager(context)
        systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
    }

    @Test
    fun testSendNotification_CreatesNotification() {
        val question = Question(
            question_id = 1,
            question_title = "How are you feeling?",
            answers = listOf("Happy", "Sad", "Neutral"),
            time_after_start = 5,
            frequency = 3,
            time_between_repetitions = 10
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notifications = shadowNotificationManager.allNotifications

        assertTrue(notifications.isNotEmpty())
        assertEquals(1, notifications.size)
    }

    @Test
    fun testSendNotification_HasCorrectContent() {
        val question = Question(
            question_id = 2,
            question_title = "What is your energy level?",
            answers = listOf("Low", "Medium", "High"),
            time_after_start = 10,
            frequency = 2,
            time_between_repetitions = 15
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notification = shadowNotificationManager.allNotifications[0]

        val shadowNotification = shadowOf(notification)
        assertEquals("SmartPlay Question", shadowNotification.contentTitle)
        assertEquals("What is your energy level?", shadowNotification.contentText)
    }

    @Test
    fun testSendNotification_HasCorrectNumberOfActions() {
        val question = Question(
            question_id = 3,
            question_title = "Choose an option",
            answers = listOf("Option 1", "Option 2", "Option 3"),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notification = shadowNotificationManager.allNotifications[0]

        assertEquals(3, notification.actions.size)
    }

    @Test
    fun testSendNotification_ActionsHaveCorrectLabels() {
        val question = Question(
            question_id = 4,
            question_title = "Select mood",
            answers = listOf("Happy", "Sad", "Angry"),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notification = shadowNotificationManager.allNotifications[0]

        assertEquals("Happy", notification.actions[0].title)
        assertEquals("Sad", notification.actions[1].title)
        assertEquals("Angry", notification.actions[2].title)
    }

    @Test
    fun testSendNotification_WithManyAnswers() {
        val answers = (1..5).map { "Answer $it" }
        val question = Question(
            question_id = 5,
            question_title = "Many options",
            answers = answers,
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notification = shadowNotificationManager.allNotifications[0]

        assertEquals(5, notification.actions.size)
    }

    @Test
    fun testSendNotification_WithSingleAnswer() {
        val question = Question(
            question_id = 6,
            question_title = "Single option",
            answers = listOf("Only Option"),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notification = shadowNotificationManager.allNotifications[0]

        assertEquals(1, notification.actions.size)
        assertEquals("Only Option", notification.actions[0].title)
    }

    @Test
    fun testSendNotification_WithNoAnswers() {
        val question = Question(
            question_id = 7,
            question_title = "No options",
            answers = emptyList(),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notification = shadowNotificationManager.allNotifications[0]

        assertEquals(0, notification.actions.size)
    }

    @Test
    fun testSendMultipleNotifications_AllCreated() {
        val question1 = Question(1, "Question 1", listOf("A", "B"), 5, 1, 10)
        val question2 = Question(2, "Question 2", listOf("C", "D"), 5, 1, 10)
        val question3 = Question(3, "Question 3", listOf("E", "F"), 5, 1, 10)

        notificationManager.sendNotification(question1)
        notificationManager.sendNotification(question2)
        notificationManager.sendNotification(question3)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notifications = shadowNotificationManager.allNotifications

        assertEquals(3, notifications.size)
    }

    @Test
    fun testSendNotification_UniqueIDs() {
        val question1 = Question(100, "Question 1", listOf("A"), 5, 1, 10)
        val question2 = Question(200, "Question 2", listOf("B"), 5, 1, 10)

        notificationManager.sendNotification(question1)
        notificationManager.sendNotification(question2)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        assertEquals(2, shadowNotificationManager.allNotifications.size)
    }

    @Test
    fun testSendNotification_HasAutoCancel() {
        val question = Question(
            question_id = 8,
            question_title = "Test auto cancel",
            answers = listOf("Yes", "No"),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notification = shadowNotificationManager.allNotifications[0]

        assertTrue((notification.flags and android.app.Notification.FLAG_AUTO_CANCEL) != 0)
    }

    @Test
    fun testSendNotification_WithLongQuestionTitle() {
        val longTitle = "This is a very long question title that should still be handled correctly " +
                "by the notification system without any issues or truncation problems"

        val question = Question(
            question_id = 9,
            question_title = longTitle,
            answers = listOf("Yes", "No"),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        val notification = shadowNotificationManager.allNotifications[0]

        val shadowNotification = shadowOf(notification)
        assertEquals(longTitle, shadowNotification.contentText)
    }

    @Test
    fun testSendNotification_WithSpecialCharactersInTitle() {
        val specialTitle = "Question: How's your mood? 😊 (1-10)"

        val question = Question(
            question_id = 10,
            question_title = specialTitle,
            answers = listOf("Good", "Bad"),
            time_after_start = 5,
            frequency = 1,
            time_between_repetitions = 10
        )

        notificationManager.sendNotification(question)

        val shadowNotificationManager = shadowOf(systemNotificationManager)
        assertTrue(shadowNotificationManager.allNotifications.isNotEmpty())
    }
}
