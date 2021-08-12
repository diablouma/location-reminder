package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private lateinit var remindersRepository: FakeDataSource

    private val context : Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setupSaveReminderViewModel() {
        stopKoin()

        remindersRepository = FakeDataSource()
        val reminder1 = ReminderDTO("Buy furniture", "furniture", "Shop in the park", 2.4, 1.3)
        val reminder2 = ReminderDTO("Buy tallarines", "tallarines", "Tallarines Shop", 1.4, -1.2)
        val reminder3 = ReminderDTO("Buy rice", "rice", "Rice Shop", 1.1, -1.01)

        runBlockingTest {
            remindersRepository.saveReminder(reminder1)
            remindersRepository.saveReminder(reminder2)
            remindersRepository.saveReminder(reminder3)
        }

        saveReminderViewModel =
            SaveReminderViewModel(context, remindersRepository)
    }

    @Test
    fun shouldClearlAllValues() {
        saveReminderViewModel.onClear()
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun shouldAssignTheSnackBarMessageWhenTitleIsEmpty() {
        val reminderData = ReminderDataItem(null, "description", "location", 1.23, -1.23)
        val validationResult = saveReminderViewModel.validateEnteredData(reminderData)

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
        assertThat(validationResult, `is`(false))
    }

    @Test
    fun shouldAssignTheSnackBarMessageWhenLocationIsEmpty() {
        val reminderData = ReminderDataItem("title", "description", null, 1.23, -1.23)
        val validationResult = saveReminderViewModel.validateEnteredData(reminderData)

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
        assertThat(validationResult, `is`(false))
    }

    @Test
    fun shouldReturnTrueWhenTheReminderDataIsComplete() {
        val reminderData = ReminderDataItem("title", "description", "location", 1.23, -1.23)
        val validationResult = saveReminderViewModel.validateEnteredData(reminderData)

        assertThat(validationResult, `is`(true))
    }

    @Test
    fun shouldChangeLoadingStatusWhileSavingTheReminder() {
        val reminderData = ReminderDataItem("title", "description", "location", 1.23, -1.23)

        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminderData)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
    }

    @Test
    fun shouldAssignAValueToTheToast() {
        val reminderData = ReminderDataItem("title", "description", "location", 1.23, -1.23)

        saveReminderViewModel.saveReminder(reminderData)

        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`(context.getString(R.string.reminder_saved)))
    }

    @Test
    fun shouldAssignAssignBackAsCommandWhenSavingWasSuccessful() {
        val reminderData = ReminderDataItem("title", "description", "location", 1.23, -1.23)

        saveReminderViewModel.saveReminder(reminderData)

        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue().toString(), `is`(NavigationCommand.Back.toString()))
    }
}