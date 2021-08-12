package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries() // for testing purposes, in prod code Room does not allow this, and we must not do it anyway
            .build()

        remindersLocalRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )

    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun shouldGetRemindersFromTheLocalDB() = runBlocking{
        val reminder = ReminderDTO("anyTitle", "anyDescription", "anyLocation", 1.01, 1.23)
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(reminder.id)

        result as Result.Success
        assertThat(result.data.title, CoreMatchers.`is`("anyTitle"))
        assertThat(result.data.description, CoreMatchers.`is`("anyDescription"))
        assertThat(result.data.location, CoreMatchers.`is`("anyLocation"))
        assertThat(result.data.latitude, CoreMatchers.`is`(1.01))
        assertThat(result.data.longitude, CoreMatchers.`is`(1.23))
    }

    @Test
    fun shouldDeleteAllRemindersFromDB() = runBlocking {
        val reminder = ReminderDTO("anyTitle", "anyDescription", "anyLocation", 1.01, 1.23)
        remindersLocalRepository.saveReminder(reminder)
        assertThat((remindersLocalRepository.getReminders() as Result.Success).data.size, `is`(1))
        remindersLocalRepository.deleteAllReminders()
        assertThat((remindersLocalRepository.getReminders() as Result.Success).data.size, `is`(0))
    }
}