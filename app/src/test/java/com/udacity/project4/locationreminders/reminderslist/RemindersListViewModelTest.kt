package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel

    private lateinit var remindersRepository: FakeDataSource

    @Before
    fun setupRemindersViewModel() {
        stopKoin()
        // Initialise the repository with no tasks.
        remindersRepository = FakeDataSource()
        val reminder1 = ReminderDTO("Buy furniture", "furniture", "Shop in the park", 2.4, 1.3)
        val reminder2 = ReminderDTO("Buy tallarines", "tallarines", "Tallarines Shop", 1.4, -1.2)
        val reminder3 = ReminderDTO("Buy rice", "rice", "Rice Shop", 1.1, -1.01)

        runBlockingTest {
            remindersRepository.saveReminder(reminder1)
            remindersRepository.saveReminder(reminder2)
            remindersRepository.saveReminder(reminder3)
        }

        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }

    @Test
    fun shouldSetRemindersInTheLiveDataWhenTheRepoHasReminders() {
        remindersListViewModel.loadReminders()
        val reminders = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(reminders.size, `is`(3))
    }

    @Test
    fun shouldChangeTheValueOfTheSnackBarToContainTheErrorWhenTheResultWasUnsuccessful() {
        remindersRepository.setReturnError(true)
        remindersListViewModel.loadReminders()
        assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("Error getting reminders")
        )
    }

    @Test
    fun shouldChangeLoadingFlagToFalseAfterRemindersHaveBeenRetrivedFromDB() {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun shouldSetShowNoDataAsTrueWhenRemindersAreEmpty() {
        runBlockingTest {
            remindersRepository.deleteAllReminders()
        }

        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }
}