package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = run {
        runBlockingTest { database.reminderDao().deleteAllReminders() }
        database.close()
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun shouldInsertDataInTheDatabase() {
        val reminder = ReminderDTO("AnyTitle", "AnyDescription", "anyLocation", 1.12, 0.12)

        runBlockingTest {
            database.reminderDao().saveReminder(reminder)
            assertThat(database.reminderDao().getReminderById(reminder.id)?.id, `is`(reminder.id))
        }
    }

    @Test
    fun shouldRetrieveAllReminders() {
        val reminder = ReminderDTO("AnyTitle", "AnyDescription", "anyLocation", 1.12, 0.12)
        val reminder2 = ReminderDTO("AnyTitle2", "AnyDescription2", "anyLocation2", 1.12, 0.12)
        runBlockingTest {
            database.reminderDao().saveReminder(reminder)
            database.reminderDao().saveReminder(reminder2)
            assertThat(database.reminderDao().getReminders().size, `is`(2))
        }
    }

    @Test
    fun shouldDeleteReminders() {
        val reminder = ReminderDTO("AnyTitle", "AnyDescription", "anyLocation", 1.12, 0.12)
        val reminder2 = ReminderDTO("AnyTitle2", "AnyDescription2", "anyLocation2", 1.12, 0.12)
        runBlockingTest {
            database.reminderDao().saveReminder(reminder)
            database.reminderDao().saveReminder(reminder2)
        }

        runBlockingTest {
            database.reminderDao().deleteAllReminders()
            assertThat(database.reminderDao().getReminders().size, `is`(0))
        }
    }

    @Test
    fun shouldReturnErrorWhenReminderWasNotFoundById() {
        val reminder = ReminderDTO("AnyTitle", "AnyDescription", "anyLocation", 1.12, 0.12)
        val invalidId = "AnyInvalidId"

        runBlockingTest {
            database.reminderDao().saveReminder(reminder)
            assertThat(database.reminderDao().getReminderById(invalidId), `is`(nullValue()))
        }
    }
}